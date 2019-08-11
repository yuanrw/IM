package com.github.yuanrw.im.common.test

import com.github.yuanrw.im.common.util.Encryption
import io.netty.util.CharsetUtil
import spock.lang.Specification

/**
 * Date: 2019-06-01
 * Time: 14:21
 * @author yrw
 */
class EncryptionTest extends Specification {

    def 'test encode and decode'() {
        given:
        def key = "ezxqNccrQpKA88bP"
        def initVector = "8422365539486988"
        def msg = "this is a message".getBytes(CharsetUtil.UTF_8)

        when:
        def encryptedMsg = Encryption.encrypt(key, initVector, msg)
        def decryptedMsg = Encryption.decrypt(key, initVector, encryptedMsg)

        then:
        decryptedMsg == msg
    }
}
