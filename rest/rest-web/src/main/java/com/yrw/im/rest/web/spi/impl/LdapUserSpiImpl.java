//package com.yrw.im.rest.web.spi.impl;
//
//import com.yrw.im.rest.spi.UserSpi;
//import com.yrw.im.rest.spi.domain.LdapUser;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.ldap.core.AttributesMapper;
//import org.springframework.ldap.core.ContextMapper;
//import org.springframework.ldap.core.DirContextAdapter;
//import org.springframework.ldap.core.LdapTemplate;
//import org.springframework.ldap.query.ContainerCriteria;
//import org.springframework.ldap.query.SearchScope;
//import org.springframework.ldap.support.LdapUtils;
//import org.springframework.stereotype.Service;
//
//import javax.naming.directory.Attribute;
//import javax.naming.ldap.LdapName;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.springframework.ldap.query.LdapQueryBuilder.query;
//
///**
// * Date: 2019-07-06
// * Time: 22:01
// *
// * @author yrw
// */
//@Service
//public class LdapUserSpiImpl implements UserSpi<LdapUser> {
//
//    /**
//     * ldap search base
//     */
//    @Value("${spring.ldap.base}")
//    private String base;
//
//    /**
//     * user objectClass
//     */
//    @Value("${ldap.mapping.objectClass}")
//    private String objectClassAttrName;
//
//    /**
//     * user LoginId
//     */
//    @Value("${ldap.mapping.loginId}")
//    private String loginIdAttrName;
//
//    /**
//     * user displayName
//     */
//    @Value("${ldap.mapping.userDisplayName}")
//    private String userDisplayNameAttrName;
//
//    /**
//     * email
//     */
//    @Value("${ldap.mapping.email}")
//    private String emailAttrName;
//
//    /**
//     * rdn
//     */
//    @Value("${ldap.mapping.rdnKey:}")
//    private String rdnKey;
//
//    /**
//     * memberOf
//     */
//    @Value("#{'${ldap.filter.memberOf:}'.split('\\|')}")
//    private String[] memberOf;
//
//    /**
//     * group search base
//     */
//    @Value("${ldap.group.groupBase:}")
//    private String groupBase;
//
//    /**
//     * group filter eg. (&(cn=apollo-admins)(&(member=*)))
//     */
//    @Value("${ldap.group.groupSearch:}")
//    private String groupSearch;
//
//    /**
//     * group memberShip eg. member
//     */
//    @Value("${ldap.group.groupMembership:}")
//    private String groupMembershipAttrName;
//
//    @Autowired
//    private LdapTemplate ldapTemplate;
//
//    private static final String MEMBER_OF_ATTR_NAME = "memberOf";
//    private static final String MEMBER_UID_ATTR_NAME = "memberUid";
//
//    @Override
//    public LdapUser getUser(String username, String pwd) {
//        return null;
//    }
//
//    @Override
//    public LdapUser getById(String id) {
//        if (StringUtils.isNotBlank(groupSearch)) {
//            List<LdapUser> lists = searchUserInfoByGroup(groupBase, groupSearch, null,
//                Collections.singletonList(id));
//            if (lists != null && !lists.isEmpty() && lists.get(0) != null) {
//                return lists.get(0);
//            }
//            return null;
//        } else {
//            return ldapTemplate
//                .searchForObject(ldapQueryCriteria().and(loginIdAttrName).is(id), ldapUserInfoMapper);
//        }
//    }
//
//    private ContextMapper<LdapUser> ldapUserInfoMapper = (ctx) -> {
//        DirContextAdapter contextAdapter = (DirContextAdapter) ctx;
//        LdapUser ldapUser = new LdapUser();
//        ldapUser.setId(contextAdapter.getStringAttribute(loginIdAttrName));
//        ldapUser.setEmail(contextAdapter.getStringAttribute(emailAttrName));
//        ldapUser.setUsername(contextAdapter.getStringAttribute(userDisplayNameAttrName));
//        return ldapUser;
//    };
//
//    /**
//     * query condition
//     */
//    private ContainerCriteria ldapQueryCriteria() {
//        ContainerCriteria criteria = query()
//            .searchScope(SearchScope.SUBTREE)
//            .where("objectClass").is(objectClassAttrName);
//        if (memberOf.length > 0 && !StringUtils.isEmpty(memberOf[0])) {
//            ContainerCriteria memberOfFilters = query().where(MEMBER_OF_ATTR_NAME).is(memberOf[0]);
//            Arrays.stream(memberOf).skip(1)
//                .forEach(filter -> memberOfFilters.or(MEMBER_OF_ATTR_NAME).is(filter));
//            criteria.and(memberOfFilters);
//        }
//        return criteria;
//    }
//
//    /**
//     * search user by group
//     *
//     * @param groupBase   group search base
//     * @param groupSearch group filter
//     * @param keyword     user search keywords
//     * @param userIds     user id list
//     */
//    private List<LdapUser> searchUserInfoByGroup(String groupBase, String groupSearch,
//                                                 String keyword, List<String> userIds) {
//
//        return ldapTemplate
//            .searchForObject(groupBase, groupSearch, ctx -> {
//                String[] members = ((DirContextAdapter) ctx).getStringAttributes(groupMembershipAttrName);
//
//                if (!MEMBER_UID_ATTR_NAME.equals(groupMembershipAttrName)) {
//                    List<LdapUser> ldapUsers = new ArrayList<>();
//                    for (String item : members) {
//                        LdapName ldapName = LdapUtils.newLdapName(item);
//                        LdapName memberRdn = LdapUtils.removeFirst(ldapName, LdapUtils.newLdapName(base));
//                        if (keyword != null) {
//                            String rdnValue = LdapUtils.getValue(memberRdn, rdnKey).toString();
//                            if (rdnValue.toLowerCase().contains(keyword.toLowerCase())) {
//                                LdapUser ldapUser = lookupUser(memberRdn.toString(), userIds);
//                                ldapUsers.add(ldapUser);
//                            }
//                        } else {
//                            LdapUser ldapUser = lookupUser(memberRdn.toString(), userIds);
//                            if (ldapUser != null) {
//                                ldapUsers.add(ldapUser);
//                            }
//                        }
//
//                    }
//                    return ldapUsers;
//                } else {
//                    List<LdapUser> ldapUsers = new ArrayList<>();
//                    String[] memberUids = ((DirContextAdapter) ctx)
//                        .getStringAttributes(groupMembershipAttrName);
//                    for (String memberUid : memberUids) {
//                        LdapUser ldapUser = searchUserById(memberUid);
//                        if (ldapUser != null) {
//                            if (keyword != null) {
//                                if (ldapUser.getId().toLowerCase().contains(keyword.toLowerCase())) {
//                                    ldapUsers.add(ldapUser);
//                                }
//                            } else {
//                                ldapUsers.add(ldapUser);
//                            }
//                        }
//                    }
//                    return ldapUsers;
//                }
//            });
//    }
//
//    private LdapUser searchUserById(String userId) {
//        return ldapTemplate.searchForObject(query().where(loginIdAttrName).is(userId),
//            ctx -> {
//                LdapUser ldapUser = new LdapUser();
//                DirContextAdapter contextAdapter = (DirContextAdapter) ctx;
//                ldapUser.setEmail(contextAdapter.getStringAttribute(emailAttrName));
//                ldapUser.setUsername(contextAdapter.getStringAttribute(userDisplayNameAttrName));
//                ldapUser.setId(contextAdapter.getStringAttribute(loginIdAttrName));
//                return ldapUser;
//            });
//    }
//
//    /**
//     * search user info by entryDN
//     *
//     * @param member  ldap EntryDN
//     * @param userIds user ID list
//     */
//    private LdapUser lookupUser(String member, List<String> userIds) {
//        return ldapTemplate.lookup(member, (AttributesMapper<LdapUser>) attributes -> {
//            LdapUser tmp = new LdapUser();
//            Attribute emailAttribute = attributes.get(emailAttrName);
//            if (emailAttribute != null && emailAttribute.get() != null) {
//                tmp.setEmail(emailAttribute.get().toString());
//            }
//            Attribute loginIdAttribute = attributes.get(loginIdAttrName);
//            if (loginIdAttribute != null && loginIdAttribute.get() != null) {
//                tmp.setId(loginIdAttribute.get().toString());
//            }
//            Attribute userDisplayNameAttribute = attributes.get(userDisplayNameAttrName);
//            if (userDisplayNameAttribute != null && userDisplayNameAttribute.get() != null) {
//                tmp.setUsername(userDisplayNameAttribute.get().toString());
//            }
//
//            if (userIds != null) {
//                if (userIds.stream().anyMatch(c -> c.equals(tmp.getId()))) {
//                    return tmp;
//                } else {
//                    return null;
//                }
//            } else {
//                return tmp;
//            }
//        });
//    }
//}