package se.vgregion.portal.dentalgrant.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.vgregion.ldapservice.LdapUser;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Patrik Bergström
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:ldap-context.xml", "classpath:security-context-properties.xml"})
public class DentalLdapServiceIT {

    @Autowired
    private DentalLdapService dentalLdapService;

    @Test
    public void testGetLdapUserByUid() throws Exception {
        LdapUser lifra1 = dentalLdapService.getLdapUserByUid("lifra1");

        assertNotNull(lifra1);

        System.out.println(lifra1.getAttributes());
    }

    @Test
    public void testSearchByUserIdQuery() {
        List<LdapUser> lifr = dentalLdapService.searchByUserIdQuery("lifra*");

        System.out.println(lifr.size());
        assertTrue(lifr.size() == 1);
    }
}
