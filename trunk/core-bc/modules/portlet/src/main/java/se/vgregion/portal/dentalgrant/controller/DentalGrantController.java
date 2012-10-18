package se.vgregion.portal.dentalgrant.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import se.vgregion.ldapservice.LdapUser;
import se.vgregion.portal.dentalgrant.service.DentalLdapService;
import se.vgregion.portal.patient.event.PersonNummer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrik Bergström
 */
@Controller
@RequestMapping("VIEW")
public class DentalGrantController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DentalGrantController.class);

    private DentalLdapService dentalLdapService;

    @Autowired
    public DentalGrantController(DentalLdapService dentalLdapService) {
        this.dentalLdapService = dentalLdapService;
    }

    @RenderMapping
    public String showForm() {
        return "grantForm";
    }

    @ResourceMapping
    public void queryPrescriber(ResourceRequest requst, ResourceResponse response) {
        String vgrId = requst.getParameter("vgrId") + "*";

        System.out.println("vgrId " + vgrId);

        List<LdapUser> ldapUsers = dentalLdapService.searchByUserIdQuery(vgrId);

        StringBuilder json;
        if (ldapUsers.size() > 0) {
            json = new StringBuilder("{");

            for (LdapUser ldapUser : ldapUsers) {
                String foundVgrId = ldapUser.getAttributeValue("uid");
                String givenName = ldapUser.getAttributeValue("givenName");
                String sn = ldapUser.getAttributeValue("sn");
                String hsaPersonPrescriptionCode = ldapUser.getAttributeValue("hsaPersonPrescriptionCode");
                String o = ldapUser.getAttributeValue("o");
                String ou = ldapUser.getAttributeValue("ou");

                json.append(String.format("'%s':{'givenName':'%s','sn':'%s','hsaPersonPrescriptionCode':'%s','o':'%s',"
                        + "'ou':'%s'},",
                        foundVgrId, givenName, sn, hsaPersonPrescriptionCode, o, ou));
            }

            json.deleteCharAt(json.length() - 1);
            json.append("}");
        } else {
            json = new StringBuilder("{}");
        }

        OutputStream out = null;
        BufferedOutputStream bos = null;
        try {
            out = response.getPortletOutputStream();
            bos = new BufferedOutputStream(out);

            String result = json.toString();
            //String s = "{'" + vgrId + "':'" + sn + "'}";
            //            String s = "{'kalle':2,'alfons':4}";
            System.out.println(result);
            bos.write(result.getBytes());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    @ActionMapping
    public void submitForm(ActionRequest request, ActionResponse response, Model model) {
        String vgrId = request.getParameter("vgrId");
        model.addAttribute("vgrId", vgrId);

        if (request.getParameter("submitSearch") != null) {
            // It's a search request
            LdapUser ldapUser = dentalLdapService.getLdapUserByUid(vgrId);


            if (ldapUser != null) {
                addAttributesToModel(model, ldapUser);
                if (ldapUser.getAttributeValue("hsaPersonPrescriptionCode") == null) {
                    model.addAttribute("message", "Denna person är ej förskrivare och kan därför inte användas för "
                            + "intyg.");
                }
            } else {
                model.addAttribute("message", "Ingen användare hittades. Observera att du måste söka på fullständigt"
                        + " VGR-ID om du inte har javascript aktiverat.");
            }
        } else if (request.getParameter("submitGrant") != null) {
            List<String> errorMessages = new ArrayList<String>();
            model.addAttribute("errorMessages", errorMessages);

            // Validate prescriber
            LdapUser ldapUser = dentalLdapService.getLdapUserByUid(vgrId);

            if (ldapUser == null) {
                errorMessages.add("Personen med angivet VGR-ID kan inte hittas.");
//                model.addAttribute("errorMessage", );
            }

            addAttributesToModel(model, ldapUser);

            String hsaPersonPrescriptionCode = ldapUser.getAttributeValue("hsaPersonPrescriptionCode");

            if (hsaPersonPrescriptionCode == null) {
                errorMessages.add("Detta är ingen giltig förskrivare. Personen saknar förskrivarkod.");
            }

            // Validate patient personalNumber
            PersonNummer pn = PersonNummer.personummer(request.getParameter("personalNumber"));
            if (!(pn.isCheckNumberValid() && pn.isDayValid() && pn.isMonthValid())) {
                errorMessages.add("Patientens personnummer är inte giltigt.");
            }

        } else {
            throw new RuntimeException("This should not happen. Fix it.");
        }
    }

    private void addAttributesToModel(Model model, LdapUser ldapUser) {
        String givenName = ldapUser.getAttributeValue("givenName");
        String sn = ldapUser.getAttributeValue("sn");
        String hsaPersonPrescriptionCode = ldapUser.getAttributeValue("hsaPersonPrescriptionCode");

        model.addAttribute("firstName", givenName);
        model.addAttribute("lastName", sn);

        if (hsaPersonPrescriptionCode == null) {
            model.addAttribute("hsaPersonPrescriptionCode", "Ej förskrivare");
        } else {
            model.addAttribute("hsaPersonPrescriptionCode", hsaPersonPrescriptionCode);
        }
    }
}
