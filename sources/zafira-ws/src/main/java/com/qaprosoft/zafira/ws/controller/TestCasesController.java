package com.qaprosoft.zafira.ws.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;

import org.apache.commons.lang3.ArrayUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import springfox.documentation.annotations.ApiIgnore;

import com.qaprosoft.zafira.dbaccess.dao.mysql.search.SearchResult;
import com.qaprosoft.zafira.dbaccess.dao.mysql.search.TestCaseSearchCriteria;
import com.qaprosoft.zafira.dbaccess.model.Project;
import com.qaprosoft.zafira.dbaccess.model.TestCase;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.services.ProjectService;
import com.qaprosoft.zafira.services.services.TestCaseService;
import com.qaprosoft.zafira.ws.dto.TestCaseType;
import com.qaprosoft.zafira.ws.swagger.annotations.ResponseStatusDetails;

@Controller
@Api(value = "Test cases operations")
@RequestMapping("tests/cases")
public class TestCasesController extends AbstractController
{
	@Autowired
	private Mapper mapper;
	
	@Autowired
	private TestCaseService testCaseService;
	
	@Autowired
	private ProjectService projectService;

	@ApiIgnore
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView index()
	{
		return new ModelAndView("tests/cases/index");
	}

	@ApiIgnore
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "metrics", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView metrics()
	{
		return new ModelAndView("tests/cases/metrics");
	}

	@ApiIgnore
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody SearchResult<TestCase> searchTestCases(@RequestBody TestCaseSearchCriteria sc) throws ServiceException
	{
		return testCaseService.searchTestRuns(sc);
	}

	@ResponseStatusDetails
	@ApiOperation(value = "Create test case", nickname = "createTestCase", code = 200, httpMethod = "POST",
			notes = "Creates a new test case or updates existing one.", response = TestCaseType.class, responseContainer = "TestCaseType")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody TestCaseType createTestCase(@RequestBody @Valid TestCaseType testCase, @RequestHeader(value="Project", required=false) String projectName) throws ServiceException
	{
		testCase.setProject(projectService.getProjectByName(projectName));
		return mapper.map(testCaseService.createOrUpdateCase(mapper.map(testCase, TestCase.class)), TestCaseType.class);
	}

	@ResponseStatusDetails
	@ApiOperation(value = "Create multiple test cases", nickname = "createTestCases", code = 200, httpMethod = "POST",
			notes = "Creates new test cases or updates existing.", response = java.util.List.class, responseContainer = "TestCase")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="batch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody TestCaseType [] createTestCases(@RequestBody @Valid TestCaseType [] tcs, @RequestHeader(value="Project", required=false) String projectName) throws ServiceException
	{
		if(!ArrayUtils.isEmpty(tcs))
		{
			Project project = projectService.getProjectByName(projectName);
			TestCase [] testCases = new TestCase[tcs.length];
			for(int i = 0; i < tcs.length; i++)
			{
				tcs[i].setProject(project);
				testCases[i] = mapper.map(tcs[i], TestCase.class);
			}
			testCases = testCaseService.createOrUpdateCases(testCases);
			for(int i = 0; i < testCases.length; i++)
			{
				tcs[i] = mapper.map(testCases[i], TestCaseType.class);
			}
			return tcs;
		}
		else
		{
			return new TestCaseType[0]; 
		}
	}
}