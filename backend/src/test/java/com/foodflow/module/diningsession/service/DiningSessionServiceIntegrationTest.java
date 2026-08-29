package com.foodflow.module.diningsession.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.foodflow.common.context.LoginContext;
import com.foodflow.common.context.LoginInfo;
import com.foodflow.common.enums.ActiveFlagEnum;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.LoginTypeEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.enums.UserStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.mapper.DiningSessionMapper;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.mapper.DiningTableMapper;
import com.foodflow.module.user.entity.User;
import com.foodflow.module.user.mapper.UserMapper;

/**
 * 直接开台的最小真实业务回归：通过 Spring 上下文调用 Service，并检查 MySQL 最终状态。
 */
@SpringBootTest
@Transactional
class DiningSessionServiceIntegrationTest {
    private static final AtomicInteger DATA_SEQUENCE = new AtomicInteger();

    @Autowired
    private DiningSessionService diningSessionService;

    @Autowired
    private DiningTableMapper diningTableMapper;

    @Autowired
    private DiningSessionMapper diningSessionMapper;

    @Autowired
    private UserMapper userMapper;

    @AfterEach
    void clearLoginContext() {
        LoginContext.clear();
    }

    @Test
    void opensFreeTableAndPersistsWaitingSession() {
        User user = insertUser();
        DiningTable table = insertFreeTable();
        LoginContext.set(userLogin(user.getId()));

        DiningSessionVO result = diningSessionService.checkInTable(table.getId());

        assertThat(result.getTableId()).isEqualTo(table.getId());
        assertThat(result.getTableNo()).isEqualTo(table.getTableNo());
        assertThat(result.getSessionStatus()).isEqualTo(DiningSessionStatusEnum.WAITING.getCode());
        assertThat(result.getTableStatus()).isEqualTo(TableStatusEnum.WAITING.getCode());

        DiningTable savedTable = diningTableMapper.selectById(table.getId());
        DiningSession savedSession = diningSessionMapper.selectById(result.getSessionId());
        assertThat(savedTable.getStatus()).isEqualTo(TableStatusEnum.WAITING);
        assertThat(savedTable.getCurrentSessionId()).isEqualTo(result.getSessionId());
        assertThat(savedSession.getUserId()).isEqualTo(user.getId());
        assertThat(savedSession.getTableId()).isEqualTo(table.getId());
        assertThat(savedSession.getStatus()).isEqualTo(DiningSessionStatusEnum.WAITING);
        assertThat(savedSession.getActiveFlag()).isEqualTo(ActiveFlagEnum.ACTIVE);
    }

    @Test
    void rejectsOpeningTableThatIsAlreadyUsed() {
        User firstUser = insertUser();
        User secondUser = insertUser();
        DiningTable table = insertFreeTable();

        LoginContext.set(userLogin(firstUser.getId()));
        DiningSessionVO firstSession = diningSessionService.checkInTable(table.getId());

        LoginContext.set(userLogin(secondUser.getId()));
        assertThatThrownBy(() -> diningSessionService.checkInTable(table.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该桌位已被使用");

        DiningTable savedTable = diningTableMapper.selectById(table.getId());
        assertThat(savedTable.getStatus()).isEqualTo(TableStatusEnum.WAITING);
        assertThat(savedTable.getCurrentSessionId()).isEqualTo(firstSession.getSessionId());
        assertThat(diningSessionMapper.selectCount(
                new QueryWrapper<DiningSession>()
                        .eq("table_id", table.getId())))
                .isEqualTo(1);
    }

    @Test
    void rejectsOpeningAnotherTableWhenUserAlreadyHasActiveSession() {
        User user = insertUser();
        DiningTable firstTable = insertFreeTable();
        DiningTable secondTable = insertFreeTable();

        LoginContext.set(userLogin(user.getId()));
        diningSessionService.checkInTable(firstTable.getId());

        assertThatThrownBy(() -> diningSessionService.checkInTable(secondTable.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前用户已存在用餐会话");

        DiningTable savedSecondTable = diningTableMapper.selectById(secondTable.getId());
        assertThat(savedSecondTable.getStatus()).isEqualTo(TableStatusEnum.FREE);
        assertThat(savedSecondTable.getCurrentSessionId()).isNull();
        assertThat(diningSessionMapper.selectCount(
                new QueryWrapper<DiningSession>()
                        .eq("user_id", user.getId())
                        .eq("active_flag", ActiveFlagEnum.ACTIVE)))
                .isEqualTo(1);
    }

    private User insertUser() {
        int sequence = DATA_SEQUENCE.incrementAndGet();
        User user = User.builder()
                .phone("139" + String.format("%08d", sequence))
                .password("test-password")
                .nickname("集成测试用户" + sequence)
                .status(UserStatusEnum.NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return user;
    }

    private DiningTable insertFreeTable() {
        int sequence = DATA_SEQUENCE.incrementAndGet();
        DiningTable table = DiningTable.builder()
                .tableNo("IT" + String.format("%04d", sequence))
                .capacity(4)
                .locationDesc("集成测试桌位")
                .status(TableStatusEnum.FREE)
                .build();
        diningTableMapper.insert(table);
        return table;
    }

    private LoginInfo userLogin(Long userId) {
        return LoginInfo.builder()
                .userId(userId)
                .loginType(LoginTypeEnum.USER)
                .build();
    }
}
