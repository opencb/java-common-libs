var tables = [
{
	name:"SNP_TABLE", 
	colNames:["SNP ID","chromosome","start","end","strand","allele","weight","transcript consequence type","consequence type","sequence"],
	colTypes:["term","text","number","number","text","text","number","text","text","text"],
	colVisibility:[1,1,1,1,1,1,1,1,0,1],
	colOrder:[0,1,2,3,4,5,6,7,8,9]
},
{
	name:"SNPEFFECT_STRUCTURE",
	colNames : ["SNP ID","SNP location","alleles","ID","trancript stable ID","gene stable ID","consequence type","splice site type","allele1","score1","sequence1","allele2","score2","sequence2"],
	colTypes : ["term","text","text","text","text","text","text","text","text","text","pvalue","text","text","pvalue","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14]
},
{
	name:"PROCESSING",
	colNames : ["SNP ID","SNP location","alleles","ID","trancript stable ID","gene stable ID","consequence type","splice site type","allele1","score1","sequence1","allele2","score2","sequence2"],
	colTypes: ["term","text","text","text","text","text","text","text","text","text","pvalue","text","text","pvalue","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14]
},
{
	name:"FUNCTIONAL",
	colNames : ["SNP ID","SNP location","alleles","ID","trancript stable ID","gene stable ID","consequence type","splice site type","allele1","score1","sequence1","allele2","score2","sequence2"],
	colTypes : ["term","text","text","text","text","text","text","text","text","text","pvalue","text","text","pvalue","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14]
},
{
	name : "OMEGA",
	colNames : ["SNP ID","SNP location","alleles","transcript stable ID","aminoacid change","aminoacid enviroment","aminoacid relative position","wSlr","slr p-value","wBayesian","bayesianModel","extrapolated"],
	colTypes : ["term","text","text","term","text","text","number","number","text","text","text","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11]
},
{
	name : "TRANSFAC",
	colNames : ["SNP ID","SNP location","alleles","ID","gene stable ID","factor ID","factor name","sequence","sequence length","factor relative start","factor relative end","snp relative position","core match","matrix match","allele1","allele2","effect"],
	colTypes : ["term","text","text","term","term","text","text","text","number","number","number","number","number","number","text","text","text"],
	colVisibility : [1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]
},
{
	name : "JASPAR",
	colNames : ["SNP ID","SNP location","alleles","factorName","gene stable ID","relative start","relative end","chromosome","start","end","strand","score","sequence"],
	colTypes : ["term","text","text","text","term","number","number","text","number","number","text","number","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,1,0],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12]
},
{
	name : "OREGANNO",
	colNames : ["SNP ID","SNP location","alleles","ID","name","chromosome","start","end","strand","land mark type","gene name","gene ID","gene source","tf name","tf ID","tf source","pubmed ID"],
	colTypes : ["term","text","text","text","text","text","number","number","text","text","text","term","text","text","text","text","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]
},
{
	name : "MIRNA_SEQUENCE",
	colNames : ["SNP ID","SNP location","alleles","accession","miRNA ID","status","sequence"],
	colTypes : ["term","text","text","text","text","text","text"],
	colVisibility : [1,1,1,1,1,1,0],
	colOrder : [0,1,2,3,4,5,6]
},
{
	name : "MIRNA_TARGET",
	colNames : ["SNP ID","SNP location","alleles","miRNA ID","transcript stable ID","chromosome","start","end","strand","score","p-value"],
	colTypes : ["term","text","text","text","term","text","number","number","text","number","pvalue"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10]
},
{
	name : "TRIPLEX",
	colNames : ["SNP ID","SNP location","alleles","ID","gene stable ID","relative start","relative end","chromosome","start","end","strand","length","sequence"],
	colTypes : ["term","text","text","number","term","number","number","text","number","number","text","number","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,0,0],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12]
},
{
	name : "CONSERVED_REGION",
	colNames : ["SNP ID","SNP location","alleles","ID","chromosome","start","end","strand","length","sequence"],
	colTypes : ["term","text","text","text","text","number","number","text","number","text"],
	colVisibility : [1,1,1,1,1,1,1,1,0,0],
	colOrder : [0,1,2,3,4,5,6,7,8,9]
},
{
	name : "SPLICE_SITE",
	colNames : ["SNP ID","SNP location","alleles","ID","trancript stable ID","gene stable ID","consequence type","splice site type","allele1","score1","sequence1","allele2","score2","sequence2"],
	colTypes : ["term","text","text","text","term","term","text","text","text","text","number","text","text","number","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,0,1,1,0],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14]
},
{
	name : "ESE",
	colNames : ["SNP ID","SNP location","alleles","ID","transcript stable ID","gene stable ID","ese ID","ese relative start","ese relative end","snp relative position","score allele1","score allele2","effect","allele"],
	colTypes : ["term","text","text","text","term","term","text","text","number","number","number","number","number","text","text"],
	colVisibility : [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14]
},
{
	name : "ESS",
	colNames : ["SNP ID","SNP location","alleles","ID","transcript stable ID","gene stable ID","sequence","start","end","snp absolute position","allele"],
	colTypes : ["term","text","text","text","term","term","text","number","number","number","text"],
	colVisibility : [1,1,1,1,1,1,0,1,1,1,1],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10]
},
{
	name : "CONSEQUENCE_TYPE_VARIANTS",
	colNames : ["Chrom", "Position", "Reference", "Alternative", "Feature ID", "Ext Name", "Feature Type", "Biotype","8","9","10","11","12","13","14","15","16","17","18","Consequence Type","20","21","22","23","24","25"],
	colTypes : ["number","number","text","text","text","text","text","text","text","text","text","text","text","text","text","text","text","text","text","text","text","text","text","number","text","text"],
	colVisibility : [1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0],
	colOrder : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]
}
];