/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.epm.client.json;

import java.util.List;

/**
 * Utility class for serialize JSON messages (create project).
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
public class DockerComposeCreateProject {

    String name;
    String yml;
    String env;

    public DockerComposeCreateProject(String name, String yml,
            List<String> envList) {
        this.name = name;
        this.yml = yml;

        if (envList.size() == 1) {
            this.env = envList.get(0);
        } else {

            StringBuilder sb = new StringBuilder();
            for (String var : envList) {
                sb.append(var).append("\n");
            }
            this.env = sb.toString();
        }
    }

    public DockerComposeCreateProject(String name, String yml) {
        this.name = name;
        this.yml = yml;
    }

    public String getName() {
        return name;
    }

    public String getYml() {
        return yml;
    }

    public String getEnv() {
        return env;
    }

    @Override
    public String toString() {
        return "DockerComposeCreateProject [getName()=" + getName()
                + ", getYml()=" + getYml() + ", getEnv()=" + getEnv() + "]";
    }

}
