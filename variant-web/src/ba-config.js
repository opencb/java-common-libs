//OPENCGA_HOST = "http://ws.bioinfo.cipf.es/opencga/rest";
OPENCGA_HOST = "http://ws-beta.bioinfo.cipf.es/opencga/rest";

if(window.location.host.indexOf("fsalavert")!=-1 ||
   window.location.host.indexOf("rsanchez")!=-1 ||
   window.location.host.indexOf("imedina")!=-1 ||
   window.location.href.indexOf("http://bioinfo.cipf.es/apps-beta")!=-1
){
//	OPENCGA_HOST = "http://mem15:8080/opencga/rest";
//	OPENCGA_HOST = "http://ws-beta.bioinfo.cipf.es/opencga/rest";
//	OPENCGA_HOST = "http://ws-beta.bioinfo.cipf.es/opencgabeta/rest";
}

SUITE_INFO = '<div style=" width: 800px;">'
    + '<h1>Overview</h1><br>'
    + '<span align="justify">PATHiWAYS is a web tool for the interpretation of the consequences of the combined changes in expression levels of genes in the context of signaling pathways. Specifically, this tool allows the user to identify the stimulus-response subpathways that are significantly activated or deactivated in the typical case/control experiment. PATHiWAYS identifies all the stimulus-response subpathways of KEGG signaling pathways, calculates the probability of activation of each one, based on the individual gene expression values and identifies those with a significant differential activity between the two conditions compared.</span>'
    + '<br><br><br>'
    + '<p align="justify"><h1>Note</h1><br>This web application makes an intensive use of new web technologies and standards like HTML5, so browsers that are fully supported for this site are: Chrome 14+, Firefox 7+, Safari 5+ and Opera 11+. Older browser like Chrome13-, Firefox 5- or Internet Explorer 9 may rise some errors. Internet Explorer 6 and 7 are no supported at all.</p>'
    + '</div>';
