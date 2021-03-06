package io.elastest.etm.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.TestSuiteXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.InstrumentedByEnum;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerService2 {

    private static final Logger logger = LoggerFactory
            .getLogger(DockerService2.class);

    private static String appImage = "elastest/test-etm-javasutrepo",
            checkImage = "elastest/etm-check-service-up";
    private static final Map<String, String> createdContainers = new HashMap<>();

    @Value("${logstash.host:#{null}}")
    private String logstashHost;

    @Value("${elastest.docker.network}")
    private String elastestNetwork;

    @Autowired
    public UtilTools utilTools;

    @PreDestroy
    public void removeAllContainers() {
        DockerClient dockerClient = getDockerClient();
        logger.info("Stopping started containers...");
        for (Map.Entry<String, String> entry : createdContainers.entrySet()) {
            String containerId = entry.getKey();
            String containerName = entry.getValue();
            try {
                stopDockerContainer(containerId, dockerClient);
                removeDockerContainer(containerId, dockerClient);
                logger.info(containerName + " removed");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadBasicServices(DockerExecution dockerExec) throws Exception {
        configureDocker(dockerExec);
        dockerExec.setNetwork(elastestNetwork);
    }

    public DockerClient getDockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder().build();
        return DockerClientBuilder.getInstance(config).build();
    }

    public void insertCreatedContainer(String containerId,
            String containerName) {
        createdContainers.put(containerId, containerName);
    }

    /**************************/
    /***** Config Methods *****/
    /**************************/

    public void configureDocker(DockerExecution dockerExec) {
        DockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder().build();
        dockerExec.setDockerClient(
                DockerClientBuilder.getInstance(config).build());
    }

    public String runDockerContainer(DockerClient dockerClient,
            String imageName, List<String> envs, String containerName,
            String targetContainerName, String networkName, Ports portBindings,
            int listenPort) throws TJobStoppedException {
        try {
            dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback()).awaitSuccess();
        } catch (InternalServerErrorException isee) {
            if (imageExistsLocally(imageName, dockerClient)) {
                logger.info("Docker image exits locally.");
            } else {
                throw isee;
            }
        } catch (DockerClientException e) {
            logger.info("Error on Pulling " + imageName
                    + " image. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        }
        CreateContainerResponse container = dockerClient
                .createContainerCmd(imageName).withName(containerName)
                .withEnv(envs).withNetworkMode(networkName)
                .withExposedPorts(ExposedPort.tcp(listenPort))
                .withPortBindings(portBindings).withPublishAllPorts(true)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        createdContainers.put(container.getId(), containerName);

        logger.info("Id del contenedor:" + container.getId());

        return container.getId();
    }

    public void removeDockerContainer(String containerId,
            DockerClient dockerClient) {
        dockerClient.removeContainerCmd(containerId).exec();
        createdContainers.remove(containerId);
    }

    public void stopDockerContainer(String containerId,
            DockerClient dockerClient) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void stopDockerContainer(DockerClient dockerClient,
            String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    /****************************/
    /***** Starting Methods *****/
    /****************************/

    public String getSutName(DockerExecution dockerExec) {
        return "sut_" + dockerExec.getExecutionId();
    }

    public void createSutContainer(DockerExecution dockerExec)
            throws TJobStoppedException {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        String sutImage = appImage;

        // Environment variables (optional)
        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        // Get Parameters and insert into Env Vars
        for (Parameter parameter : sut.getParameters()) {
            envVar = parameter.getName() + "=" + parameter.getValue();
            envList.add(envVar);
        }

        if (sut.getSutType() == SutTypeEnum.MANAGED) {
            sutImage = sut.getSpecification();
            envVar = "REPO_URL=none";
        } else {
            envVar = "REPO_URL=" + sut.getSpecification();
        }
        envList.add(envVar);

        // Load Log Config
        logger.info(
                "Sut " + dockerExec.getExecutionId() + " image: " + sutImage);
        String sutName = getSutName(dockerExec);
        LogConfig logConfig = getLogConfig(5001, "sut_", dockerExec);

        // Pull Image
        try {
            dockerExec.getDockerClient().pullImageCmd(sutImage)
                    .exec(new PullImageResultCallback()).awaitSuccess();
        } catch (InternalServerErrorException isee) {
            if (imageExistsLocally(sutImage, dockerExec.getDockerClient())) {
                logger.info("Docker image exits locally.");
            } else {
                logger.error("Error pulling the image: {}", isee.getMessage());
                throw isee;
            }
        } catch (DockerClientException e) {
            logger.info(
                    "Error on Pulling SuT image. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        }

        // Create Container
        dockerExec.setAppContainer(dockerExec.getDockerClient()
                .createContainerCmd(sutImage).withEnv(envList)
                .withLogConfig(logConfig).withName(sutName)
                .withNetworkMode(dockerExec.getNetwork()).exec());

        String appContainerId = dockerExec.getAppContainer().getId();
        dockerExec.setAppContainerId(appContainerId);
    }

    public void startSutcontainer(DockerExecution dockerExec) {
        String sutName = getSutName(dockerExec);
        String sutContainerId = dockerExec.getAppContainerId();

        dockerExec.getDockerClient().startContainerCmd(sutContainerId).exec();
        createdContainers.put(sutContainerId, sutName);
    }

    public String getCheckName(DockerExecution dockerExec) {
        return "check_" + dockerExec.getExecutionId();
    }

    public void checkSut(DockerExecution dockerExec, String ip, String port)
            throws Exception {
        String envVar = "IP=" + ip;
        String envVar2 = "PORT=" + port;
        ArrayList<String> envList = new ArrayList<>();
        envList.add(envVar);
        envList.add(envVar2);

        try {
            dockerExec.getDockerClient().pullImageCmd(checkImage)
                    .exec(new PullImageResultCallback()).awaitSuccess();
        } catch (InternalServerErrorException isee) {
            if (imageExistsLocally(checkImage, dockerExec.getDockerClient())) {
                logger.info("Docker image exits locally.");
            } else {
                logger.error("Error pulling the image: {}", isee.getMessage());
                throw isee;
            }
        } catch (DockerClientException e) {
            logger.info(
                    "Error on Pulling CheckSut. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        }
        String checkName = getCheckName(dockerExec);
        String checkContainerId = dockerExec.getDockerClient()
                .createContainerCmd(checkImage).withEnv(envList)
                .withName(checkName).withNetworkMode(dockerExec.getNetwork())
                .exec().getId();
        dockerExec.getDockerClient().startContainerCmd(checkContainerId).exec();
        createdContainers.put(checkContainerId, checkName);

        try {
            dockerExec.getDockerClient().waitContainerCmd(checkContainerId)
                    .exec(new WaitContainerResultCallback()).awaitStatusCode();
            logger.info("Sut is ready " + dockerExec.getExecutionId());

        } catch (DockerClientException e) {
            logger.info(
                    "Error on Waiting for CheckSut. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        } catch (Exception e) {
        }
    }

    /****************/
    /***** Test *****/
    /****************/

    public String getTestName(DockerExecution dockerExec) {
        return "test_" + dockerExec.getExecutionId();
    }

    public void createTestContainer(DockerExecution dockerExec)
            throws TJobStoppedException {
        try {

            logger.info("Starting test " + dockerExec.getExecutionId());
            String testImage = dockerExec.gettJobexec().getTjob()
                    .getImageName();
            logger.info("host: " + getHostIp(dockerExec));

            // Environment variables (optional)
            ArrayList<String> envList = new ArrayList<>();
            String envVar;

            // Get TestSupportService Env Vars
            for (Map.Entry<String, String> entry : dockerExec.gettJobexec()
                    .getTssEnvVars().entrySet()) {
                envVar = entry.getKey() + "=" + entry.getValue();
                envList.add(envVar);
            }

            // Get Parameters and insert into Env Vars
            for (Parameter parameter : dockerExec.gettJobexec()
                    .getParameters()) {
                envVar = parameter.getName() + "=" + parameter.getValue();
                envList.add(envVar);
            }

            if (dockerExec.isWithSut() && InstrumentedByEnum.ADMIN != dockerExec
                    .gettJobexec().getTjob().getSut().getInstrumentedBy()) {
                envList.add("APP_IP=" + dockerExec.getSutExec().getUrl());
                envList.add("ET_SUT_HOST=" + dockerExec.getSutExec().getIp());
            }

            // Commands (optional)
            ArrayList<String> cmdList = new ArrayList<>();
            String commands = dockerExec.gettJobexec().getTjob().getCommands();
            if (commands != null && !commands.isEmpty()) {
                cmdList.add("sh");
                cmdList.add("-c");
                cmdList.add(commands);
            }

            LogConfig logConfig = getLogConfig(5000, "test_", dockerExec);

            try {
                dockerExec.getDockerClient().pullImageCmd(testImage)
                        .exec(new PullImageResultCallback()).awaitSuccess();
            } catch (InternalServerErrorException isee) {
                if (imageExistsLocally(testImage,
                        dockerExec.getDockerClient())) {
                    logger.info("Docker image exits locally.");
                } else {
                    logger.info("Docker image does not exits locally.");
                    throw isee;
                }
            } catch (DockerClientException e) {
                logger.info(
                        "Error on Pulling TJob image. Probably because the user has stopped the execution");
                throw new TJobStoppedException();
            }
            String testName = getTestName(dockerExec);
            CreateContainerResponse testContainer = dockerExec.getDockerClient()
                    .createContainerCmd(testImage).withEnv(envList)
                    .withLogConfig(logConfig).withName(testName)
                    .withCmd(cmdList).withNetworkMode(dockerExec.getNetwork())
                    .exec();

            String testContainerId = testContainer.getId();

            dockerExec.setTestcontainer(testContainer);
            dockerExec.setTestContainerId(testContainerId);
        } catch (DockerClientException dce) {
            throw new TJobStoppedException();
        } catch (TJobStoppedException dce) {
            throw new TJobStoppedException();
        }
    }

    public List<ReportTestSuite> startTestContainer(DockerExecution dockerExec)
            throws TJobStoppedException {
        try {
            String testContainerId = dockerExec.getTestContainerId();
            String testName = getTestName(dockerExec);

            dockerExec.getDockerClient().startContainerCmd(testContainerId)
                    .exec();
            createdContainers.put(testContainerId, testName);

            int code = dockerExec.getDockerClient()
                    .waitContainerCmd(testContainerId)
                    .exec(new WaitContainerResultCallback()).awaitStatusCode();
            dockerExec.setTestContainerExitCode(code);
            logger.info("Test container ends with code " + code);

            return getTestResults(dockerExec);

        } catch (DockerClientException dce) {
            throw new TJobStoppedException();
        }
    }

    public LogConfig getLogConfig(int port, String tagPrefix,
            DockerExecution dockerExec) {

        logstashHost = getLogstashHost(dockerExec);

        logger.info("Logstash IP to send logs from containers: {}",
                logstashHost);

        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("syslog-address", "tcp://" + logstashHost + ":" + port);
        configMap.put("tag", tagPrefix + dockerExec.getExecutionId() + "_exec");

        LogConfig logConfig = new LogConfig();
        logConfig.setType(LoggingType.SYSLOG);
        logConfig.setConfig(configMap);

        return logConfig;
    }

    public String getLogstashHost(DockerExecution dockerExec) {
        if (logstashHost == null) {
            return getHostIpByNetwork(dockerExec, dockerExec.getNetwork());
        }
        return logstashHost;
    }

    /*********************************/
    /***** End execution methods *****/
    /*********************************/

    public void endContainer(DockerExecution dockerExec, String containerName) {
        if (existsContainer(containerName, dockerExec)) {
            String containerId = getContainerIdByName(containerName,
                    dockerExec);
            try {
                logger.info("Stopping " + containerName + " container");
                dockerExec.getDockerClient().stopContainerCmd(containerId)
                        .exec();
            } catch (DockerClientException e) {
                // throw new TJobStoppedException();
            } catch (NotModifiedException e) {
                logger.info(
                        "Container " + containerName + " is already stopped");
            } catch (Exception e) {
                logger.info(
                        "Error during stop " + containerName + " container");
            } finally {
                try {
                    logger.info("Removing " + containerName + " container");
                    dockerExec.getDockerClient().removeContainerCmd(containerId)
                            .exec();
                } catch (DockerClientException e) {
                    // throw new TJobStoppedException();
                } catch (Exception e) {
                    logger.info("Error during remove " + containerName
                            + "container");
                }
                createdContainers.remove(containerId);
            }
        } else {
            logger.info("Could not end " + containerName
                    + " container -> Not started.");
        }
    }

    /*****************/
    /***** Utils *****/
    /*****************/

    public String getContainerIp(String containerId,
            DockerExecution dockerExec) {
        String ip = dockerExec.getDockerClient()
                .inspectContainerCmd(containerId).exec().getNetworkSettings()
                .getNetworks().get(dockerExec.getNetwork()).getIpAddress();
        return ip.split("/")[0];
    }

    public String getContainerIpByNetwork(String containerId, String network) {
        DockerClient client = getDockerClient();

        String ip = client.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().get(network).getIpAddress();
        return ip.split("/")[0];
    }

    public String getNetworkName(String containerId,
            DockerClient dockerClient) {
        return (String) dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().keySet().toArray()[0];
    }

    public String getHostIp(DockerExecution dockerExec) {
        return dockerExec.getDockerClient().inspectNetworkCmd()
                .withNetworkId(dockerExec.getNetwork()).exec().getIpam()
                .getConfig().get(0).getGateway();
    }

    public String getHostIpByNetwork(DockerExecution dockerExec,
            String network) {
        return dockerExec.getDockerClient().inspectNetworkCmd()
                .withNetworkId(network).exec().getIpam().getConfig().get(0)
                .getGateway();
    }

    public boolean imageExist(String imageName, DockerExecution dockerExec) {
        return !dockerExec.getDockerClient().searchImagesCmd(imageName).exec()
                .isEmpty();
    }

    public boolean imageExistsLocally(String imageName,
            DockerClient dockerClient) {
        boolean imageExists = false;
        try {
            dockerClient.inspectImageCmd(imageName).exec();
            imageExists = true;
        } catch (NotFoundException nfe) {
            imageExists = false;
        }
        return imageExists;
    }

    public void insertIntoNetwork(String networkId, String containerId) {
        DockerClient client = getDockerClient();
        client.connectToNetworkCmd().withNetworkId(networkId)
                .withContainerId(containerId).exec();
    }

    /***************************/
    /***** Get TestResults *****/
    /***************************/
    public InputStream getFileFromContainer(String containerName,
            String fileName, DockerExecution dockerExec) {
        InputStream inputStream = null;
        if (existsContainer(getTestName(dockerExec), dockerExec)) {
            inputStream = dockerExec.getDockerClient()
                    .copyArchiveFromContainerCmd(containerName, fileName)
                    .exec();
        }
        return inputStream;
    }

    public boolean existsContainer(String containerName,
            DockerExecution dockerExec) {
        boolean exists = true;
        try {
            dockerExec.getDockerClient().inspectContainerCmd(containerName)
                    .exec();
        } catch (NotFoundException e) {
            exists = false;
        }
        return exists;
    }

    public String getContainerIdByName(String containerName,
            DockerExecution dockerExec) {
        String id = "";
        if (existsContainer(containerName, dockerExec)) {
            try {

                InspectContainerResponse response = dockerExec.getDockerClient()
                        .inspectContainerCmd(containerName).exec();
                id = response.getId();
            } catch (Exception e) {

            }
        }
        return id;
    }

    private List<ReportTestSuite> getTestResults(DockerExecution dockerExec) {
        List<ReportTestSuite> testSuites = null;
        String resultsPath = dockerExec.gettJobexec().getTjob()
                .getResultsPath();

        if (resultsPath != null && !resultsPath.isEmpty()) {
            try {
                InputStream inputStream = getFileFromContainer(
                        dockerExec.getTestContainerId(), resultsPath,
                        dockerExec);

                String result = IOUtils.toString(inputStream,
                        StandardCharsets.UTF_8);
                result = repairXML(result);

                TestSuiteXmlParser testSuiteXmlParser = new TestSuiteXmlParser(
                        null);
                InputStream byteArrayIs = new ByteArrayInputStream(
                        result.getBytes());
                testSuites = testSuiteXmlParser
                        .parse(new InputStreamReader(byteArrayIs, "UTF-8"));

            } catch (IOException e) {
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        return testSuites;
    }

    private String repairXML(String result) {
        String head = "<testsuite ";
        String foot = "</testsuite>";

        String[] splitedResult = result.split(head);
        String repaired = head + splitedResult[1];

        splitedResult = repaired.split(foot);
        repaired = splitedResult[0] + foot;

        return repaired;
    }

}
