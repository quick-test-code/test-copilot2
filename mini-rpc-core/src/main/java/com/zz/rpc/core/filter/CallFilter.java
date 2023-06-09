package com.zz.rpc.core.filter;

import com.zz.rpc.core.rpc.RpcRequest;
import com.zz.rpc.core.rpc.RpcResponse;
import org.springframework.core.NamedThreadLocal;

public class CallFilter extends AbstractFilter {

    private NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<>("StopWatch-StartTime");

    @Override
    public void before(RpcRequest request) {
        long beginTime = System.currentTimeMillis();
        startTimeThreadLocal.set(beginTime);
    }

    @Override
    public void after(RpcResponse response) {
        long endTime = System.currentTimeMillis();
        long beginTime = startTimeThreadLocal.get();
        long consumeTime = endTime - beginTime;
    }
}
