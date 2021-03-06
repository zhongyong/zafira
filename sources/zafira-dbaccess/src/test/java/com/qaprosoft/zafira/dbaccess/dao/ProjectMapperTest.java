/*******************************************************************************
 * Copyright 2013-2019 Qaprosoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.zafira.dbaccess.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.qaprosoft.zafira.dbaccess.dao.mysql.application.ProjectMapper;
import com.qaprosoft.zafira.models.db.Project;

@Test
@ContextConfiguration("classpath:com/qaprosoft/zafira/dbaccess/dbaccess-test.xml")
public class ProjectMapperTest extends AbstractTestNGSpringContextTests {
    private static final boolean ENABLED = false;

    private static final Project PROJECT = new Project() {
        private static final long serialVersionUID = 1L;
        {
            setName("n1");
            setDescription("d1");
        }
    };

    @Autowired
    private ProjectMapper projectMapper;

    @Test(enabled = ENABLED)
    public void createProject() {
        projectMapper.createProject(PROJECT);
        assertNotEquals(PROJECT.getId(), 0, "Project ID must be set up by autogenerated keys");
    }

    @Test(enabled = ENABLED, dependsOnMethods = { "createProject" })
    public void getProjectById() {
        checkProject(projectMapper.getProjectById(PROJECT.getId()));
    }

    @Test(enabled = ENABLED, dependsOnMethods = { "createProject" })
    public void getProjectByName() {
        checkProject(projectMapper.getProjectByName(PROJECT.getName()));
    }

    @Test(enabled = ENABLED, dependsOnMethods = { "createProject" })
    public void getAllProjects() {
        checkProject(projectMapper.getAllProjects().get(0));
    }

    @Test(enabled = ENABLED, dependsOnMethods = { "createProject" })
    public void updateProject() {
        PROJECT.setName("n2");
        PROJECT.setDescription("d2");

        projectMapper.updateProject(PROJECT);

        checkProject(projectMapper.getProjectById(PROJECT.getId()));
    }

    /**
     * Turn this in to delete car after all tests
     */
    private static final boolean DELETE_ENABLED = true;

    @Test(enabled = ENABLED && DELETE_ENABLED, dependsOnMethods = { "createProject", "getProjectById", "getProjectByName", "getAllProjects",
            "updateProject" })
    public void deleteProjectById() {
        projectMapper.deleteProjectById(PROJECT.getId());

        assertNull(projectMapper.getProjectById(PROJECT.getId()));
    }

    private void checkProject(Project project) {
        assertEquals(project.getName(), PROJECT.getName(), "Name must match");
        assertEquals(project.getDescription(), PROJECT.getDescription(), "Description must match");
    }
}
