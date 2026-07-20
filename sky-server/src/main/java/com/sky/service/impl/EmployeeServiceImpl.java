package com.sky.service.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对密码进行md5加密，然后与数据库中的密码（md5加密后的）进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //1、将DTO转换为实体对象
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置默认密码，并用md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置默认状态为启用
        employee.setStatus(StatusConstant.ENABLE);

        // 设置创建时间和更新时间为当前时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置创建人和更新人
        // 从BaseContext中获取当前员工id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        // 在数据库中新增员工
        employeeMapper.insert(employee);


    }

    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        //利用PageHelper分页查询(页码（默认1），每页显示记录数（10）)
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        //查询符合条件的员工分页数据
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        //获取总记录数
        long total = page.getTotal();

        //获取当前页数据
        List<Employee> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 启用/停用员工
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
//        Employee employee = new Employee();
//        employee.setId(id);
//        employee.setStatus(status);


        Employee employee = Employee.builder()
                            .id(id)
                            .status(status)
                            .build();

        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee selectById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        //加密一下查询到的密码，防止泄露原始密码
        employee.setPassword("******");
        return employee;
    }

    /**
     * 更新员工
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        //将DTO转换为实体对象
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置更新人和更新时间为当前时间
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setUpdateTime(LocalDateTime.now());

        //更新数据库
        employeeMapper.update(employee);
    }

}
