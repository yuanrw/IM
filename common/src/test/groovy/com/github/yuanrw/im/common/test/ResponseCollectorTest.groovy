package com.github.yuanrw.im.common.test

import com.github.yuanrw.im.common.domain.ResponseCollector
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.common.util.IdWorker
import com.github.yuanrw.im.protobuf.generate.Internal
import spock.lang.Specification
import spock.lang.Timeout

import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * Date: 2019-05-31
 * Time: 18:49
 * @author yrw
 */
class ResponseCollectorTest extends Specification {

    @Timeout(value = 4500, unit = TimeUnit.MILLISECONDS)
    def "test wait time out"() {
        given:
        def msgResponseCollector = new ResponseCollector(Duration.ofSeconds(2), "test")

        when:
        def timeStart = System.currentTimeMillis()
        msgResponseCollector.future.get()

        then:
        thrown(ExecutionException)

        def timeEnd = System.currentTimeMillis()
        timeEnd - timeStart >= 1800
        println(timeEnd - timeStart)
    }

    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    def "test when completed"() {
        given:
        def collector = new ArrayList<Internal.InternalMsg>()
        def msgResponseCollector = new ResponseCollector<Internal.InternalMsg>(Duration.ofSeconds(2), "test")
        msgResponseCollector.getFuture().whenComplete({ m, e -> collector.add(m) })

        when:
        Internal.InternalMsg msg = Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody("123")
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.CLIENT)
                .build()

        msgResponseCollector.getFuture().complete(msg)
        msgResponseCollector.getFuture().get()

        then:
        collector.size() == 1

        Internal.InternalMsg res = collector.get(0)
        res.getVersion() == 1
        res.getMsgType() == Internal.InternalMsg.MsgType.ACK
        res.getMsgBody() == "123"
    }
}
