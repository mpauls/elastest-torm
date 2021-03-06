package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * SutSpecification
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
@ApiModel(description = "SUT definition.")
public class SutSpecification {

    public interface SutView {
    }

    @Id
    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class,
            BasicAttTJobExec.class })
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @JsonProperty("name")
    private String name = null;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @Column(name = "specification", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("specification")
    private String specification = null;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @JsonProperty("description")
    private String description = null;

    @JsonProperty("sutExecution")
    @OneToMany(mappedBy = "sutSpecification", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<SutExecution> sutExecution = null;

    @JsonView({ SutView.class })
    @JsonProperty("project")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project = null;

    @JsonProperty("tjobs")
    @OneToMany(mappedBy = "sut")
    private List<TJob> tJobs;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @Column(name = "sutType", nullable = false)
    @JsonProperty("sutType")
    private SutTypeEnum sutType;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "eimConfig")
    private EimConfig eimConfig;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @Column(name = "instrumentalize")
    @JsonProperty("instrumentalize")
    private boolean instrumentalize = false;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @Column(name = "currentSutExec")
    @JsonProperty("currentSutExec")
    private Long currentSutExec = null;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @Column(name = "instrumentedBy", nullable = false)
    @JsonProperty("instrumentedBy")
    private InstrumentedByEnum instrumentedBy;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @Column(name = "port")
    @JsonProperty("port")
    private String port = null;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @Column(name = "managedDockerType", nullable = false)
    @JsonProperty("managedDockerType")
    private ManagedDockerType managedDockerType;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @JsonProperty("mainService")
    private String mainService = null;

    @JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
    @ElementCollection
    @CollectionTable(name = "SutParameter", joinColumns = @JoinColumn(name = "SutSpecification"))
    private List<Parameter> parameters;

    public SutSpecification() {
    }

    public SutSpecification(Long id, String name, String specification,
            String description, Project project, List<TJob> tJobs,
            SutTypeEnum sutType, boolean instrumentalize, Long currentSutExec,
            InstrumentedByEnum instrumentedBy, String port,
            ManagedDockerType managedDockerType) {
        this.id = id == null ? 0 : id;
        this.name = name;
        this.specification = specification;
        this.description = description;
        this.project = project;
        this.tJobs = tJobs;
        this.sutType = sutType;
        this.instrumentalize = instrumentalize;
        this.currentSutExec = currentSutExec;
        this.instrumentedBy = instrumentedBy;
        this.port = port;
        this.managedDockerType = managedDockerType;
    }

    public enum SutTypeEnum {
        MANAGED("MANAGED"),

        REPOSITORY("REPOSITORY"),

        DEPLOYED("DEPLOYED");

        private String value;

        SutTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static SutTypeEnum fromValue(String text) {
            for (SutTypeEnum b : SutTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum InstrumentedByEnum {
        WITHOUT("WITHOUT"),

        ELASTEST("ELASTEST"),

        ADMIN("ADMIN");

        private String value;

        InstrumentedByEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static InstrumentedByEnum fromValue(String text) {
            for (InstrumentedByEnum b : InstrumentedByEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum ManagedDockerType {
        IMAGE("IMAGE"),

        COMPOSE("COMPOSE");

        private String value;

        ManagedDockerType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ManagedDockerType fromValue(String text) {
            for (ManagedDockerType b : ManagedDockerType.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /**
     * Get id
     * 
     * @return id
     **/
    @ApiModelProperty(value = "Value that identifies the SUT Specification.")

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
    }

    /**
     * Get name
     * 
     * @return name
     **/
    @ApiModelProperty(example = "sut definition 1", required = true, value = "Name of the SUT.")
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Specification
     * 
     * @return specification
     **/
    @ApiModelProperty(example = "https://github.com/elastest/elastest-torm.git", required = true, value = "URL of the GitHub repository where the SUT code is stored (not necessary if the ImageName field is filled)")
    @NotNull
    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    /**
     * Get desc
     * 
     * @return desc
     **/
    @ApiModelProperty(example = "My Web Application", value = "Brief description of a SUT")
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * Get sutExecution
     * 
     * @return sutExecution
     **/
    @ApiModelProperty(value = "List of the SUT Executions of a SUT")
    public List<SutExecution> getSutExecution() {
        return sutExecution;
    }

    public void setSutExecution(List<SutExecution> sutExecution) {
        this.sutExecution = sutExecution;
    }

    public SutExecution addSuTExecution(SutExecution sutExecution) {
        getSutExecution().add(sutExecution);
        sutExecution.setSutSpecification(this);

        return sutExecution;
    }

    public SutExecution removeSuTExecution(SutExecution sutExecution) {
        getSutExecution().remove(sutExecution);
        sutExecution.setSutSpecification(null);

        return sutExecution;
    }

    /**
     * Get project
     * 
     * @return project
     **/
    @ApiModelProperty(required = true, value = "Project to which the SUT is associated", example = "{ id:\"1\" }")
    @NotNull
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Get tJobs
     * 
     * @return tJobs
     **/

    @Valid
    @ApiModelProperty(value = "List of TJobs associated to a SUT Specification")
    public List<TJob> getTJobs() {
        return tJobs;
    }

    public void setTJobs(List<TJob> tJobs) {
        this.tJobs = tJobs;
    }

    public TJob addTJob(TJob tJob) {
        getTJobs().add(tJob);
        tJob.setSut(this);

        return tJob;
    }

    public TJob removeTJob(TJob tJob) {
        getTJobs().remove(tJob);
        tJob.setSut(null);

        return tJob;
    }

    /**
     * Get sutType
     * 
     * @return sutType
     **/

    public SutTypeEnum getSutType() {
        return sutType;
    }

    public void setSutType(SutTypeEnum sutType) {
        this.sutType = sutType;
    }

    /**
     * Get eimConfig
     * 
     * @return eimConfig
     **/

    @ApiModelProperty(value = "EIM configuration")
    public EimConfig getEimConfig() {
        return eimConfig;
    }

    public void setEimConfig(EimConfig eimConfig) {
        this.eimConfig = eimConfig;
    }

    /**
     * Get instrumentalize
     * 
     * @return instrumentalize
     **/

    public boolean isinstrumentalize() {
        return instrumentalize;
    }

    public void setinstrumentalize(boolean instrumentalize) {
        this.instrumentalize = instrumentalize;
    }

    /**
     * Get currentSutExec
     * 
     * @return currentSutExec
     **/

    public Long getCurrentSutExec() {
        return currentSutExec;
    }

    public void setCurrentSutExec(Long currentSutExec) {
        this.currentSutExec = currentSutExec;
    }

    /**
     * Get instrumentedBy
     * 
     * @return instrumentedBy
     **/

    public InstrumentedByEnum getInstrumentedBy() {
        return instrumentedBy;
    }

    public void setInstrumentedBy(InstrumentedByEnum instrumentedBy) {
        this.instrumentedBy = instrumentedBy;
    }

    /**
     * Get port
     * 
     * @return port
     **/

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Get managedDockerType
     * 
     * @return managedDockerType
     **/

    public ManagedDockerType getManagedDockerType() {
        return managedDockerType;
    }

    public void setManagedDockerType(ManagedDockerType managedDockerType) {
        this.managedDockerType = managedDockerType;
    }

    /**
     * Get mainService
     * 
     * @return mainService
     **/

    public String getMainService() {
        return mainService;
    }

    public void setMainService(String mainService) {
        this.mainService = mainService;
    }

    /**
     * Get parameters
     * 
     * @return parameters
     **/

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    // Other methods

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SutSpecification sutSpecification = (SutSpecification) o;
        return Objects.equals(this.id, sutSpecification.id)
                && Objects.equals(this.name, sutSpecification.name)
                && Objects.equals(this.specification,
                        sutSpecification.specification)
                && Objects.equals(this.description,
                        sutSpecification.description)
                && Objects.equals(this.project, sutSpecification.project)
                && Objects.equals(this.sutType, sutSpecification.sutType)
                && Objects.equals(this.eimConfig, sutSpecification.eimConfig)
                && Objects.equals(this.instrumentalize,
                        sutSpecification.instrumentalize)
                && Objects.equals(this.currentSutExec,
                        sutSpecification.currentSutExec)
                && Objects.equals(this.instrumentedBy,
                        sutSpecification.instrumentedBy)
                && Objects.equals(this.port, sutSpecification.port)
                && Objects.equals(this.managedDockerType,
                        sutSpecification.managedDockerType)
                && Objects.equals(this.mainService,
                        sutSpecification.mainService)
                && Objects.equals(this.parameters, sutSpecification.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, specification, description, project,
                sutType, eimConfig, instrumentedBy, port, managedDockerType,
                mainService, parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SutSpecification {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    specification: ").append(toIndentedString(specification))
                .append("\n");
        sb.append("    description: ").append(toIndentedString(description))
                .append("\n");
        sb.append("    project: ").append(toIndentedString(project))
                .append("\n");
        sb.append("    sutType: ").append(toIndentedString(sutType))
                .append("\n");
        sb.append("    eimConfig: ").append(toIndentedString(eimConfig))
                .append("\n");
        sb.append("    instrumentalize: ")
                .append(toIndentedString(instrumentalize)).append("\n");
        sb.append("    currentSutExec: ")
                .append(toIndentedString(currentSutExec)).append("\n");
        sb.append("    instrumentedBy: ")
                .append(toIndentedString(instrumentedBy)).append("\n");
        sb.append("    port: ").append(toIndentedString(port)).append("\n");
        sb.append("    managedDockerType: ")
                .append(toIndentedString(managedDockerType)).append("\n");
        sb.append("    mainService: ").append(toIndentedString(mainService))
                .append("\n");
        sb.append("    parameters: ").append(toIndentedString(parameters))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }

}
