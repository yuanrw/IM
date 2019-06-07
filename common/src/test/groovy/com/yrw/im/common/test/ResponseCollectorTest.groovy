package com.yrw.im.common.test

import com.yrw.im.common.domain.ResponseCollector
import com.yrw.im.common.util.IdWorker
import com.yrw.im.proto.generate.Internal
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

    @Timeout(value = 2500, unit = TimeUnit.MILLISECONDS)
    def "test wait time out"() {
        given:
        def msgResponseCollector = new ResponseCollector(Duration.ofSeconds(2))

        when:
        def timeStart = System.currentTimeMillis()
        msgResponseCollector.future.get()

        then:
        thrown(ExecutionException)

        def timeEnd = System.currentTimeMillis()
        timeEnd - timeStart >= 2000
    }

    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    def "test when completed"() {
        given:
        def collector = new ArrayList<Internal.InternalMsg>()
        def msgResponseCollector = new ResponseCollector<Internal.InternalMsg>(Duration.ofSeconds(2))
        msgResponseCollector.getFuture().whenComplete({ m, e -> collector.add(m) })

        when:
        Internal.InternalMsg msg = Internal.InternalMsg.newBuilder()
                .setVersion(1)
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
