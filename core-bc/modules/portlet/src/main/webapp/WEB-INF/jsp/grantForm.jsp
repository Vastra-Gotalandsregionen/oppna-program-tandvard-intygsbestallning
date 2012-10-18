<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<portlet:defineObjects/>

<style type="text/css">
    table.search-result td, table.search-result th {
        padding-right: 15px;
        border-bottom: gray solid thin;
    }

    table.search-result {
        width: 95%;
    }

    .dental-grant-wrap table td {
        padding-right: 15px;
        height: 23px;
    }

    .hover-option {
        background-color: #add8e6;
        cursor: pointer;
    }

    #optionList {
        overflow-y: scroll; max-height: 500px;
        overflow-x: hidden;
    }

   /* .search-result-dialog {
        max-width: 70%;
        min-width: 100px;
    }*/

</style>

<portlet:actionURL var="submit">
    <portlet:param name="action" value="submit"/>
</portlet:actionURL>

<div class="dental-grant-wrap">

    <c:if test="${not empty message}">
        <span class="portlet-msg-info">${message}</span>
    </c:if>
    <c:if test="${not empty errorMessages}">
        <c:forEach items="${errorMessages}" var="errorMessage">
            <span class="portlet-msg-error">${errorMessage}</span>
        </c:forEach>
    </c:if>

    <form action="${submit}" method="post">
        <table>
            <tr>
                <td>Personnummer patient:</td>
                <td><input type="text" name="personalNumber" value="${patientPersonalNumber}"/></td>
            </tr>
            <tr>
                <td>Patientkategori:</td>
                <td><select>
                    <option value="1">Kategori 1</option>
                    <option value="2">Kategori 2</option>
                </select></td>

            </tr>
        </table>
        <fieldset>
            <legend>
                Förskrivare
            </legend>
            Fyll i VGR-ID, eller åtminstone de tre första bokstäverna, och tryck på Sök:
            <table>
                <tr>
                    <td>VGR-id:</td>
                    <td><input id="<portlet:namespace />vgrId" type="text" name="vgrId" value="${vgrId}"/></td>
                    <td>
                        <input type="submit" name="submitSearch" id="<portlet:namespace />searchPrescriberButton" value="Sök"/>
                    </td>
                </tr>
                <tr>
                    <td>Förnamn:</td>
                    <td><span id="<portlet:namespace />firstName">${firstName}</span></td>
                </tr>
                <tr>
                    <td>Efternamn:</td>
                    <td><span id="<portlet:namespace />lastName">${lastName}</span></td>
                </tr>
                <tr>
                    <td>Förskrivarkod:</td>
                    <td><span id="<portlet:namespace />hsaPersonPrescriptionCode">${hsaPersonPrescriptionCode}</span></td>
                </tr>
            </table>
        </fieldset>
        <input type="submit" name="submitGrant" value="Skicka intyg"/>
    </form>
</div>

<portlet:resourceURL var="queryPrescriber">
    <portlet:param name="action" value="queryPrescriber"/>
</portlet:resourceURL>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/dentalGrant.js?a=c"></script>

<script type="text/javascript">

    AUI().ready('aui-base', 'aui-dialog', 'aui-overlay-manager', function (A) {
        var searchButton = A.one('#<portlet:namespace />searchPrescriberButton');
        var vgrIdInput = A.one('#<portlet:namespace />vgrId');
        addOnClickListener('<%= queryPrescriber %>', searchButton, A, vgrIdInput, '<portlet:namespace />');
    });

</script>