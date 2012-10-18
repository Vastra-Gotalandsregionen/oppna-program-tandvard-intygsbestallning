package se.vgregion.portal.dentalgrant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.vgregion.ldapservice.LdapService;
import se.vgregion.ldapservice.LdapUser;

import java.util.Arrays;
import java.util.List;

/**
 * @author Patrik Bergström
 */
@Service
public class DentalLdapService {

    private LdapService ldapService;
    private LdapService simpleLdapService;

    @Value("${BASE}")
    private String ldapBase;

    public DentalLdapService(LdapService ldapService, LdapService simpleLdapService) {
        this.ldapService = ldapService;
        this.simpleLdapService = simpleLdapService;
    }

    public LdapUser getLdapUserByUid(String vgrId) {
        LdapUser ldapUser = simpleLdapService.getLdapUserByUid(vgrId);

        return ldapUser;
    }

    public List<LdapUser> searchByUserIdQuery(String vgrId) {
        LdapUser[] search = ldapService.search(ldapBase, "uid=" + vgrId);

        return Arrays.asList(search);
    }

}
