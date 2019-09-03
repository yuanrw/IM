package com.github.yuanrw.im.rest.web.spi.impl;

import com.github.yuanrw.im.rest.spi.UserSpi;
import com.github.yuanrw.im.rest.spi.domain.LdapUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Date: 2019-07-06
 * Time: 22:01
 *
 * @author yrw
 */
@Service
public class LdapUserSpiImpl implements UserSpi<LdapUser> {

    @Value("${ldap.searchBase}")
    private String searchBase;

    @Value("${ldap.mapping.objectClass}")
    private String objectClassAttrName;

    @Value("${ldap.mapping.loginId}")
    private String loginIdAttrName;

    @Value("${ldap.mapping.userDisplayName}")
    private String userDisplayNameAttrName;

    @Value("${ldap.mapping.email}")
    private String emailAttrName;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public LdapUser getUser(String username, String pwd) {
        AndFilter filter = new AndFilter()
            .and(new EqualsFilter(emailAttrName, username));
        boolean authenticate = ldapTemplate.authenticate(searchBase, filter.encode(), pwd);
        return authenticate ? ldapTemplate.searchForObject(ldapQueryCriteria()
            .and(emailAttrName).is(username), ldapUserInfoMapper) : null;
    }

    @Override
    public LdapUser getById(String id) {
        return ldapTemplate.searchForObject(ldapQueryCriteria()
            .and(loginIdAttrName).is(id), ldapUserInfoMapper);
    }

    private ContextMapper<LdapUser> ldapUserInfoMapper = (ctx) -> {
        DirContextAdapter contextAdapter = (DirContextAdapter) ctx;
        LdapUser ldapUser = new LdapUser();
        ldapUser.setId(contextAdapter.getStringAttribute(loginIdAttrName));
        ldapUser.setEmail(contextAdapter.getStringAttribute(emailAttrName));
        ldapUser.setUsername(contextAdapter.getStringAttribute(userDisplayNameAttrName));
        return ldapUser;
    };

    private ContainerCriteria ldapQueryCriteria() {
        return query().searchScope(SearchScope.SUBTREE)
            .where("objectClass").is(objectClassAttrName);
    }
}