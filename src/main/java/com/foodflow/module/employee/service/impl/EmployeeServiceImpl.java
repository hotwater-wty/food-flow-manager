package com.foodflow.module.employee.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.constant.JwtClaimConstants;
import com.foodflow.common.enums.EmployeeRoleEnum;
import com.foodflow.common.enums.EmployeeStatusEnum;
import com.foodflow.common.enums.LoginTypeEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.utils.JwtUtil;
import com.foodflow.module.employee.dto.EmployeeLoginDTO;
import com.foodflow.module.employee.dto.EmployeeRegisterDTO;
import com.foodflow.module.employee.entity.Employee;
import com.foodflow.module.employee.mapper.EmployeeMapper;
import com.foodflow.module.employee.service.EmployeeService;
import com.foodflow.module.employee.vo.EmployeeLoginVO;
import com.foodflow.module.employee.vo.EmployeeRegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public EmployeeRegisterVO register(EmployeeRegisterDTO employeeRegisterDTO) {
        String phone = employeeRegisterDTO.getPhone();

        // 注册流程 1：手机号作为账号标识，先检查是否已经注册。
        Employee employee = query()
                .eq("phone", phone)
                .one();
        if (employee != null) {
            throw new BusinessException("该手机号已注册员工账号");
        }

        // 注册流程 2：密码只存 BCrypt 加密结果，不能存明文。
        String encodedPassword = passwordEncoder.encode(employeeRegisterDTO.getPassword());

        // 注册流程 3：未传姓名时，默认使用手机号作为姓名。
        if (employeeRegisterDTO.getName() == null || employeeRegisterDTO.getName().isEmpty()) {
            employeeRegisterDTO.setName(phone);
        }

        // 注册流程 4：组装员工实体，id 由数据库自增生成。
        employee = Employee.builder()
                .phone(phone)
                .password(encodedPassword)
                .role(EmployeeRoleEnum.STAFF)
                .name(employeeRegisterDTO.getName())
                .status(EmployeeStatusEnum.NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 注册流程 5：保存后 MyBatis-Plus 会把数据库生成的主键 id 回填到 employee 对象中。
        saveOrUpdate(employee);

        // 注册流程 6：封装注册响应，避免把 password 返回给前端。
        return toRegisterVO(employee);
    }

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        String phone = employeeLoginDTO.getPhone();

        // 登录流程 1：先按手机号查出员工，不能把明文密码放到 SQL 条件里比价
        Employee employee = query()
                .eq("phone", phone)
                .one();

        // 登录流程 2：使用 BCrypt 的 matches 比较明文密码和数据库中的加密密码。
        if(employee == null || !passwordEncoder.matches(employeeLoginDTO.getPassword(), employee.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }
        // 登录流程 2.1：检查员工账号是否正常。
        if (employee.getStatus() != EmployeeStatusEnum.NORMAL) {
            throw new BusinessException("员工账号不可用");
        }

        // 登录流程 3：把后续鉴权需要的员工信息写入 JWT 载荷。
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(JwtClaimConstants.EMPLOYEE_ID, employee.getId());
        tokenMap.put(JwtClaimConstants.PHONE, employee.getPhone());
        tokenMap.put(JwtClaimConstants.LOGIN_TYPE, LoginTypeEnum.EMPLOYEE.name());
        tokenMap.put(JwtClaimConstants.ROLE, employee.getRole().name());

        // 登录流程 4：生成 Token，返回给前端；前端后续请求放入 Authorization 请求头。
        String token = JwtUtil.generateToken(tokenMap);

        // 登录流程 5：封装登录响应，注意 employeeId 和 token 需要手动设置。
        EmployeeLoginVO employeeLoginVO = toLoginVO(employee);
        employeeLoginVO.setEmployeeId(employee.getId());
        employeeLoginVO.setToken(token);
        return employeeLoginVO;
    }

    private EmployeeLoginVO toLoginVO(Employee employee) {
        EmployeeLoginVO employeeLoginVO = BeanUtil.copyProperties(employee, EmployeeLoginVO.class);
        employeeLoginVO.setEmployeeId(employee.getId());
        employeeLoginVO.setStatus(employee.getStatus().getCode());
        return employeeLoginVO;
    }

    private EmployeeRegisterVO toRegisterVO(Employee employee) {
        EmployeeRegisterVO employeeRegisterVO = BeanUtil.copyProperties(employee, EmployeeRegisterVO.class);
        employeeRegisterVO.setEmployeeId(employee.getId());
        employeeRegisterVO.setStatus(employee.getStatus().getCode());
        return employeeRegisterVO;
    }
}
