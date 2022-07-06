package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    /**
     * Method calculates the Reporting Structure a specific employee
     * @param String id
     * @return ReportingStructure
     */
    @Override
    public ReportingStructure numberOfReports(String id) {
        Employee employee = read(id);

        ReportingStructure rs = new ReportingStructure();

        int numberOfReports = numReportsRecursive(employee.getDirectReports(), employee);

        rs.setEmployee(employee);
        rs.setNumberOfReports(numberOfReports);

        return new ReportingStructure(employee, numberOfReports);
    }

    /**
     * Recursive method to calculate the direct reports of the above employees direct reports
     * @param List<Employee> es
     * @param Employee e
     * @return int
     */
    public int numReportsRecursive(List<Employee> es, Employee e){
        if(es == null){
            return 0;
        }
        int reports = es.size();

        int childReports = 0;
        List<Employee> tempList = new ArrayList<Employee>();
        for(int i = 0; i < reports; i++){
            
            Employee temp = read(es.get(i).getEmployeeId());
            tempList.add(temp);
            childReports += numReportsRecursive(temp.getDirectReports(), tempList.get(i));
        }

        e.setDirectReports(tempList);

        return reports + childReports;
    }
}
