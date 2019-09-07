package com.github.yuanrw.im.client.test

import com.github.yuanrw.im.client.context.impl.MemoryRelationCache
import com.github.yuanrw.im.client.service.ClientRestService
import com.github.yuanrw.im.common.domain.po.RelationDetail
import spock.lang.Specification

/**
 * Date: 2019-09-07
 * Time: 07:57
 * @author yrw
 */
class RelationCacheTest extends Specification {

    def "test add relation"() {
        given:
        def clientRestService = Mock(ClientRestService)
        def relationCache = new MemoryRelationCache(clientRestService)

        when:
        def relation = new RelationDetail()
        relation.setUserId1("123")
        relation.setUserId2("456")
        relation.setUsername1("123 name")
        relation.setUsername2("456 name")
        relationCache.addRelation(relation)

        then:
        def relationDetail = relationCache.getRelation("123", "456", "token")

        relationDetail.userId1 == "123"
        relationDetail.userId2 == "456"
        relationDetail.username1 == "123 name"
        relationDetail.username2 == "456 name"

        0 * clientRestService.relation(_ as String, _ as String, _ as String)
    }

    def "test add relations"() {
        given:
        def clientRestService = Mock(ClientRestService)
        def relationCache = new MemoryRelationCache(clientRestService)

        when:
        def list = new LinkedList()
        def relation1 = new RelationDetail()
        relation1.setUserId1("123")
        relation1.setUserId2("456")
        relation1.setUsername1("123 name")
        relation1.setUsername2("456 name")

        def relation2 = new RelationDetail()
        relation2.setUserId1("131")
        relation2.setUserId2("068")
        relation2.setUsername1("131 name")
        relation2.setUsername2("068 name")

        list.add(relation1)
        list.add(relation2)

        relationCache.addRelations(list)

        then:
        def relationDetail1 = relationCache.getRelation("123", "456", "token")
        relationDetail1.userId1 == "123"
        relationDetail1.userId2 == "456"
        relationDetail1.username1 == "123 name"
        relationDetail1.username2 == "456 name"

        def relationDetail2 = relationCache.getRelation("131", "068", "token")
        relationDetail2.userId1 == "131"
        relationDetail2.userId2 == "068"
        relationDetail2.username1 == "131 name"
        relationDetail2.username2 == "068 name"
    }

    def "test get relation not exist"() {
        given:
        def clientRestService = Mock(ClientRestService)
        def relationCache = new MemoryRelationCache(clientRestService)

        when:
        relationCache.getRelation("123", "456", "token")

        then:
        relationCache.getRelation("123", "456", "token") == null
        1 * clientRestService.relation("123", "456", "token")
    }
}