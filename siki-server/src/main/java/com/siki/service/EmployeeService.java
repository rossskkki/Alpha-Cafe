package com.siki.service;

import com.siki.dto.EmployeeDTO;
import com.siki.dto.EmployeeLoginDTO;
import com.siki.dto.EmployeePageQueryDTO;
import com.siki.entity.Employee;
import com.siki.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用或停用员工
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 修改员工
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     */
    void fixpwd(String oldPassword, String newPassword);
}
