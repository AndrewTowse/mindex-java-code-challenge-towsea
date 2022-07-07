package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;

    private String numberOfReportsUrl;

    private String compensationUrl;
    private String compensationIdUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";

        // TASK 1 test URLs
        numberOfReportsUrl = "http://localhost:" + port + "/numberOfReports/{id}";

        // TASK 2 Test URLs
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }


    //TASK 1 TESTS
    /**
     * Tests the response of numberOfReports endpoint
     */
    @Test
    public void testGetNumberOfReports(){
    
        //CREATE REPORTS FOR THOMAS BAR AND BE SURE THEY PASS CREATE CHECK
        Employee testEmployee1 = new Employee();
        testEmployee1.setFirstName("Alex");
        testEmployee1.setLastName("Web");
        testEmployee1.setDepartment("Basketball");
        testEmployee1.setPosition("Manager");

        Employee reportEmp1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        assertNotNull(reportEmp1.getEmployeeId());
        assertEmployeeEquivalence(testEmployee1, reportEmp1);

        testEmployee1.setFirstName("Madison");
        testEmployee1.setLastName("Crouse");
        testEmployee1.setDepartment("Elementary School");
        testEmployee1.setPosition("Kidergarten");

        Employee reportEmp2 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        assertNotNull(reportEmp2.getEmployeeId());
        assertEmployeeEquivalence(testEmployee1, reportEmp2);

        // CREATE THOMAS
        testEmployee1.setFirstName("Thomas");
        testEmployee1.setLastName("Bar");
        testEmployee1.setDepartment("FBA");
        testEmployee1.setPosition("Software Engineer");

        // ADD JUST EMPLOYEE ID TO THOMAS DIRECT REPORTS
        List<Employee> tempReports = new ArrayList<Employee>();
        Employee tempE = new Employee();
        tempE.setEmployeeId(reportEmp1.getEmployeeId());
        tempReports.add(tempE);
        tempE = new Employee();
        tempE.setEmployeeId(reportEmp2.getEmployeeId());
        tempReports.add(tempE);
        testEmployee1.setDirectReports(tempReports);

        Employee reportEmp3 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        assertNotNull(reportEmp3.getEmployeeId());
        assertEmployeeEquivalence(testEmployee1, reportEmp3);

        // CREATE REPORTING STRUCTURE FOR THOMAS
        ReportingStructure testStructure = new ReportingStructure(reportEmp3, 2);
        List<Employee> trlist = new ArrayList<Employee>();
        trlist.add(reportEmp1);
        trlist.add(reportEmp2);
        testStructure.getEmployee().setDirectReports(trlist);

        ReportingStructure readStructure = restTemplate.getForEntity(numberOfReportsUrl, ReportingStructure.class, testStructure.getEmployee().getEmployeeId()).getBody();
        assertReportingStructureEquivalence(testStructure, readStructure);
    }

    /**
     * Takes two separate ReportingStructures and ensures they are equal
     * @param ReportingStructure expected
     * @param ReportingStructure actual
     */
    private static void assertReportingStructureEquivalence(ReportingStructure expected, ReportingStructure actual) {
        assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
        assertEquals(expected.getNumberOfReports(), actual.getNumberOfReports());
        assertRSERecursive(expected.getEmployee().getDirectReports(), actual.getEmployee().getDirectReports());
    }

    /**
     * Method recursively checks the structure to ensure they are identical
     * @param List<Employee> esC
     * @param List<Employee> esR
     */
    private static void assertRSERecursive(List<Employee> esC, List<Employee> esR){
        if(esR == null && esC == null){
            return;
        }
        else if(esR == null || esC == null || esR.size() != esC.size()){
            throw new RuntimeException("Structures do not match");
        }

        int reports = esR.size();
        for(int i = 0; i < reports; i++){
            assertEmployeeEquivalence(esC.get(i), esR.get(i));
            assertRSERecursive(esC.get(i).getDirectReports(), esR.get(i).getDirectReports());
        }

        return;
    }

    //TASK 2 TESTS
    /**
     * Tests the create post endpoint and read get endpoint for compensation objects
     */
    @Test
    public void testCreateReadCompensation(){
        
        // CREATE EMPLOYEE TO PASS EXISTENCE CHECK
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("Andrew");
        testEmployee.setLastName("Towse");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // EMPLOYEE MUST EXIST TO ADD COMPENSATION FOR THEM
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();
        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);
        
        Compensation testComp = new Compensation();
        testComp.setEmployee(createdEmployee);
        testComp.setSalary(23000);
        testComp.setEffectiveDate("7/6/2022");

        Compensation createdComp = restTemplate.postForEntity(compensationUrl, testComp, Compensation.class).getBody();
        assertNotNull(createdComp.getEmployee().getEmployeeId());

        Compensation readComp = restTemplate.getForEntity(compensationIdUrl, Compensation.class, createdComp.getEmployee().getEmployeeId()).getBody();
        assertNotNull(readComp.getEmployee());
        assertCompensationEquivalence(createdComp, readComp);
        
    }

    /**
     * Asserts entered compensation objects are equivalent
     * @param Compensation expected
     * @param Compensation actual
     */
    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }


}
