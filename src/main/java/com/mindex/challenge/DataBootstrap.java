package com.mindex.challenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;

@Component
public class DataBootstrap {
    private static final String DATASTORE_LOCATION = "/static/employee_database.json";
    private static final String DATASTORE_LOCATION1 = "/static/compensation_database.json";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Autowired
    private ObjectMapper objectMapper1;

    @Autowired
    private ObjectMapper objectMapper2;

    @PostConstruct
    public void init() {
        InputStream inputStream1 = this.getClass().getResourceAsStream(DATASTORE_LOCATION);

        Employee[] employees = null;

        try {
            employees = objectMapper1.readValue(inputStream1, Employee[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Employee employee : employees) {
            employeeRepository.insert(employee);
        }

        // START OF COMPENSATION REPOSITORY
        InputStream inputStream2 = this.getClass().getResourceAsStream(DATASTORE_LOCATION1);

        Compensation[] compensations = null;

        try {
            compensations = objectMapper2.readValue(inputStream2, Compensation[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Compensation compensation : compensations) {
            compensationRepository.insert(compensation);
        }


    }

    // @PreDestroy
    // public void shutDown(){
    //     System.out.println("\n\n\n\n Shutting down \n\n\n\n");
    //     closeCompensationRepository();
    // }

    // public void closeCompensationRepository(){
    //     System.out.println("\n\n\n\n Closing Repo \n\n\n\n");
    //     compensationRepository.saveAll(compensationRepository.findAll());
    // }
}
