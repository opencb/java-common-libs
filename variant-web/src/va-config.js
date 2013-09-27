CELLBASE_HOST = "http://ws.bioinfo.cipf.es/cellbase/rest";
OPENCGA_HOST = "http://ws-beta.bioinfo.cipf.es/opencga/rest";

if (
    window.location.host.indexOf("localhost") != -1 ||
        window.location.host.indexOf("fsalavert") != -1 ||
        window.location.host.indexOf("rsanchez") != -1 ||
        window.location.host.indexOf("imedina") != -1 ||
        window.location.href.indexOf("http://bioinfo.cipf.es/apps-beta") != -1 ||
        window.location.protocol === "file:"
    ) {

    CELLBASE_HOST = "http://ws-beta.bioinfo.cipf.es/cellbase-server-3.0.0/rest";
    OPENCGA_HOST = "http://ws-beta.bioinfo.cipf.es/opencga-server-0.2.0/rest";
}


SUITE_INFO =  '<div style=" width: 800px;">'
    +'<h2>Overview</h2><span align="justify">VARIANT (VARIant ANalysis Tool) can report the functional properties of any variant in all the human, mouse or rat genes (and soon new model organisms will be added) and the corresponding neighborhoods. Also other non-coding extra-genic regions, such as miRNAs are included in the analysis.<br><br>	VARIANT not only reports the obvious functional effects in the coding regions but also analyzes noncoding SNVs situated both within the gene and in the neighborhood that could affect different regulatory motifs, splicing signals, and other structural elements. These include: Jaspar regulatory motifs, miRNA targets, splice sites, exonic splicing silencers, calculations of selective pressures on the particular polymorphic positions, etc.</span>'
    +'<br><br><br>'
    +'<p align="justify"><h2>Note</h2>This web application makes an intensive use of new web technologies and standards like HTML5, so browsers that are fully supported for this site are: Chrome 14+, Firefox 7+, Safari 5+ and Opera 11+. Older browser like Chrome13-, Firefox 5- or Internet Explorer 9 may rise some errors. Internet Explorer 6 and 7 are no supported at all.</p>'
    +'</div>'+
    +'<br><br><h2>Sign in</h2><p style=" width: 800px;">You must be logged in to use this Web application, you can <b><i>register</i></b> or use a <b><i>anonymous user</i></b> as shown in the following image by clicking on the <b><i>"Sign in"</i></b> button on the top bar</p><br><div style="float:left;"><img src="http://jsapi.bioinfo.cipf.es/libs/resources/img/loginhelpbutton.png"></div><img src="http://jsapi.bioinfo.cipf.es/libs/resources/img/loginhelp.png">'+
    +'';

