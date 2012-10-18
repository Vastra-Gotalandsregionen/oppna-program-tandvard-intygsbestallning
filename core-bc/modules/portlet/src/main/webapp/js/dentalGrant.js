function addOnClickListener(url, searchButton, A, vgrIdInput, namespace) {
    searchButton.on('click', function (e) {
        searchButton.set('disabled', 'disabled');
        vgrIdInput.set('disabled', 'disabled');
        e.halt();
        var items = null;
        var vgrIdSearchQuery = vgrIdInput.get('value');
        if (vgrIdSearchQuery.length < 3) {
            alert('Du måste ange minst tre tecken för att få söka.');
            searchButton.removeAttribute('disabled');
            vgrIdInput.removeAttribute('disabled');
            return;
        }
        A.io.request(url + '&vgrId=' + vgrIdSearchQuery, {
            cache:false,
            sync:false,
            timeout:3000,
            dataType:'text',
            method:'get',
            on:{
                success:function () {
                    searchButton.removeAttribute('disabled');
                    vgrIdInput.removeAttribute('disabled');

                    var jsonText = this.get('responseData');
                    items = eval('(' + jsonText + ')');

                    var options = {
//                        bodyContent: '',
                        buttons:[
                            {text:'Avbryt',
                                handler:function () {
                                    this.hide();
                                }}
                        ],
                        draggable:true
                    }

                    var html = "", htmlArray = [];
                    htmlArray.push('<div id="optionList"><table class="search-result">');
                    htmlArray.push('<tr><th>VGR-ID</th><th>Förnamn</th><th>Efternamn</th>',
                        '<th>Förvaltning</th><th>Enhet</th><th>Förskrivarkod</th><th>&nbsp;</th></tr>');

                    var count = 0;
                    for (var key in items) {
                        count++;

                        htmlArray.push('<tr class="entry-row">');
                        htmlArray.push('<td class="vgrId-column">', key, '</td>'); // vgrId

                        var personEntry = items[key];
                        htmlArray.push('<td class="givenName-column">', personEntry['givenName'], '</td>'); // attributeName : value
                        htmlArray.push('<td class="sn-column">', personEntry['sn'], '</td>'); // attributeName : value
                        htmlArray.push('<td class="o-column">', personEntry['o'], '</td>'); // attributeName : value
                        htmlArray.push('<td class="ou-column">', personEntry['ou'], '</td>'); // attributeName : value
                        var hsaPersonPrescriptionCode = personEntry['hsaPersonPrescriptionCode'];
                        if (isNaN(hsaPersonPrescriptionCode)) {
                            hsaPersonPrescriptionCode = '<i>Ej förskrivare</i>'
                        }
                        htmlArray.push('<td class="hsaPersonPrescriptionCode-column">', hsaPersonPrescriptionCode, '</td>'); // attributeName : value

                        htmlArray.push('</tr>');
                    }

                    htmlArray.push('</table>');

                    html = htmlArray.join("");

                    var dialog = new A.Dialog(
                        A.merge(
                            options,
                            {
                                centered:true,
                                //cssClass: 'search-result-dialog',
                                bodyContent:html,
                                modal:true,
                                title:'Din sökning gav ' + count + ' träffar. Välj en sökträff med förskrivarkod.',
                                width: 700
                            }
                        )
                    ).render();

                    A.one('#optionList').one('table').all('tr.entry-row').each(function (node, index, list) {
                        var hsaPersonPrescriptionCode = node.one('.hsaPersonPrescriptionCode-column').html();
                        if (isNaN(hsaPersonPrescriptionCode)) {
                            return;
                        }

                        node.on('mouseover', function (e) {
                            node.addClass('hover-option');
                            //node.set('style', 'background-color:gray');

                        });
                        node.on('mouseout', function (e) {
                            node.removeClass('hover-option');
                        });
                        node.on('click', function (e) {
                            var vgrId = node.one('.vgrId-column').html();
                            var givenName = node.one('.givenName-column').html();
                            var sn = node.one('.sn-column').html();
                            var hsaPersonPrescriptionCode = node.one('.hsaPersonPrescriptionCode-column').html();
                            A.one('#' + namespace + 'vgrId').set('value', vgrId);
                            A.one('#' + namespace + 'firstName').html(givenName);
                            A.one('#' + namespace + 'lastName').html(sn);
                            A.one('#' + namespace + 'lastName').html(sn);
                            A.one('#' + namespace + 'hsaPersonPrescriptionCode').html(hsaPersonPrescriptionCode);

                            dialog.hide();
                        })
                    }, this);

                },
                failure:function () {
                    searchButton.remove('disabled');
                    vgrIdInput.remove('disabled');
                }
            }
        });
    })
}
