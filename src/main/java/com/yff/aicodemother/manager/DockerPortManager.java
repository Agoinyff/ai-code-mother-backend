package com.yff.aicodemother.manager;

import com.yff.aicodemother.constant.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Docker 端口池管理器
 * <p>
 * 负责动态分配和释放 Docker 容器映射端口，保证并发安全、不冲突。
 * 使用 BitSet 高效管理端口占用状态。
 * <p>
 * 维护两个独立的端口池：
 * - 预览端口池：3001-3100（临时容器用）
 * - 部署端口池：4001-4100（持久容器用）
 *
 * @author yff
 */
@Slf4j
@Component
public class DockerPortManager {


    /**
     * 部署端口池的占用状态（BitSet 下标 0 对应 DEPLOY_PORT_RANGE_START）
     */
    private final BitSet deployPorts;


    /**
     * 部署端口池容量
     */
    private final int deployPoolSize;

    /**
     * 线程安全锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    public DockerPortManager() {
        this.deployPoolSize = AppConstant.DEPLOY_PORT_RANGE_END - AppConstant.DEPLOY_PORT_RANGE_START + 1;
        this.deployPorts = new BitSet(deployPoolSize);
        log.info("端口池初始化完成 -部署端口范围: {}-{}（容量:{}）",
                AppConstant.DEPLOY_PORT_RANGE_START, AppConstant.DEPLOY_PORT_RANGE_END, deployPoolSize);
    }



    /**
     * 分配一个可用的部署端口
     *
     * @return 可用端口号
     * @throws RuntimeException 当端口池满时抛出异常
     */
    public int allocateDeployPort() {
        return allocatePort(deployPorts, deployPoolSize, AppConstant.DEPLOY_PORT_RANGE_START, "部署");
    }



    /**
     * 释放部署端口
     *
     * @param port 要释放的端口号
     */
    public void releaseDeployPort(int port) {
        releasePort(deployPorts, port, AppConstant.DEPLOY_PORT_RANGE_START, "部署");
    }



    /**
     * 获取当前部署端口使用数量
     *
     * @return 已使用的部署端口数
     */
    public int getDeployPortUsage() {
        lock.lock();
        try {
            return deployPorts.cardinality();
        } finally {
            lock.unlock();
        }
    }



    /**
     * 标记某个部署端口为已占用（用于启动时恢复已存在的容器状态）
     *
     * @param port 要标记的端口号
     */
    public void markDeployPortAsUsed(int port) {
        markPortAsUsed(deployPorts, port, AppConstant.DEPLOY_PORT_RANGE_START, "部署");
    }

    // ==================== 私有方法 ====================

    /**
     * 从指定端口池分配一个端口
     */
    private int allocatePort(BitSet bitSet, int poolSize, int rangeStart, String poolName) {
        lock.lock();
        try {
            // nextClearBit 返回第一个未被占用的位（即可用端口）
            int index = bitSet.nextClearBit(0);
            if (index >= poolSize) {
                throw new RuntimeException(String.format("%s端口池已满（%d/%d），无法分配新端口",
                        poolName, bitSet.cardinality(), poolSize));
            }
            bitSet.set(index); // 标记为已占用
            int port = rangeStart + index;
            log.info("分配{}端口: {}（已使用: {}/{}）", poolName, port, bitSet.cardinality(), poolSize);
            return port;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 释放指定端口
     */
    private void releasePort(BitSet bitSet, int port, int rangeStart, String poolName) {
        lock.lock();
        try {
            int index = port - rangeStart;
            if (index < 0 || index >= bitSet.size()) {
                log.warn("尝试释放无效的{}端口: {}，忽略操作", poolName, port);
                return;
            }
            if (!bitSet.get(index)) {
                log.warn("{}端口 {} 未被占用，无需释放", poolName, port);
                return;
            }
            bitSet.clear(index); // 标记为未占用
            log.info("释放{}端口: {}（已使用: {}/{}）", poolName, port, bitSet.cardinality(),
                    deployPoolSize);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 标记端口为已占用
     */
    private void markPortAsUsed(BitSet bitSet, int port, int rangeStart, String poolName) {
        lock.lock();
        try {
            int index = port - rangeStart;
            if (index < 0 || index >= bitSet.size()) {
                log.warn("无效的{}端口: {}，忽略标记操作", poolName, port);
                return;
            }
            bitSet.set(index);
            log.info("标记{}端口 {} 为已占用", poolName, port);
        } finally {
            lock.unlock();
        }
    }
}
