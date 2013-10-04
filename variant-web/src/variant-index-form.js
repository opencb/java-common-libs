/*
 * Copyright (c) 2012 Francisco Salavert (ICM-CIPF)
 * Copyright (c) 2012 Ruben Sanchez (ICM-CIPF)
 * Copyright (c) 2012 Ignacio Medina (ICM-CIPF)
 *
 * This file is part of JS Common Libs.
 *
 * JS Common Libs is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JS Common Libs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JS Common Libs. If not, see <http://www.gnu.org/licenses/>.
 */

VariantIndexForm.prototype = new GenericFormPanel("variant");

function VariantIndexForm(webapp) {
    this.id = Utils.genId("VariantIndexForm");
    this.headerWidget = webapp.headerWidget;
    this.opencgaBrowserWidget = webapp.headerWidget.opencgaBrowserWidget;

//    this.testing = true;
}

VariantIndexForm.prototype.beforeRun = function () {

    if (this.testing) {
        console.log("Watch out!!! testing flag is on, so job will not launched.")
    }
};


VariantIndexForm.prototype.getPanels = function () {
    var items = [
        this._getBrowseForm()
    ];

    var form = Ext.create('Ext.panel.Panel', {
        margin: "15 0 5 0",
        border: false,
//		layout:{type:'vbox', align: 'stretch'},
        buttonAlign: 'center',
        width: "99%",
        //height:900,
        //width: "600",
        items: items
    });

//    Ext.getCmp(this.id + 'jobname').setValue("Example vcf 5000");
//    Ext.getCmp(this.id + 'jobdescription').setValue("VCF file with ~5000 variants");

    return [this._getExampleForm(), form];
};


VariantIndexForm.prototype._getExampleForm = function () {
    var _this = this;

    var example1 = Ext.create('Ext.Component', {
        width: 275,
        html: '<span class="u"><span class="emph u">Load example 1.</span> <span class="info s110">VCF file with ~3500 variants</span></span>',
        cls: 'dedo',
        listeners: {
            afterrender: function () {
                this.getEl().on("click", function () {
                    _this.loadExample1();
                    Ext.example.msg("Example loaded", "");
                });

            }
        }
    });

    var exampleForm = Ext.create('Ext.container.Container', {
        bodyPadding: 10,
        items: [this.note1, example1],
        defaults: {margin: '5 0 0 5'}
    });

    return exampleForm;
};

VariantIndexForm.prototype._getBrowseForm = function () {
    var _this = this;

    var note1 = Ext.create('Ext.container.Container', {
        html: '<p>Please select a VCF file from your <span class="info">server account</span> using the <span class="emph">Browse</span> button.</p>'
    });
    var note2 = Ext.create('Ext.container.Container', {
        html: '<p>Please select a PED file from your <span class="info">server account</span> using the <span class="emph">Browse</span> button.</p>'
    });

    var formBrowser = Ext.create('Ext.panel.Panel', {
        title: "Select your data",
        //cls:'panel-border-top',
        border: true,
        padding: "5 0 0 0",
        bodyPadding: 10,
        items: [
            note1,
            this.createOpencgaBrowserCmp({
                fieldLabel: 'Input VCF file:',
                dataParamName: 'vcf-file',
                id: this.id + 'vcf-file',
                mode: 'fileSelection',
                allowedTypes: ['vcf'],
                allowBlank: false
            }),
            note2,
            this.createOpencgaBrowserCmp({
                fieldLabel: 'Input PED file:',
                dataParamName: 'ped-file',
                id: this.id + 'ped-file',
                mode: 'fileSelection',
                allowedTypes: ['ped'],
                allowBlank: false
            })]
    });
    return formBrowser;
};


VariantIndexForm.prototype.loadExample1 = function () {

    Ext.getCmp(this.id + 'vcf-file').setText('<span class="emph">Example 1</span>', false);
    Ext.getCmp(this.id + 'vcf-file' + 'hidden').setValue('example_CHB.exon.2010_03.sites.fixed.vcf');


    Ext.getCmp(this.id + 'jobname').setValue("Example vcf 3500");
    Ext.getCmp(this.id + 'jobdescription').setValue("VCF file with ~3500 variants");

    Ext.getCmp("Only SNPs_" + this.id).setValue(true);
//
//
//
//	Ext.getCmp("Only SNPs_"+this.id).setValue(true);
//	this.fileBrowserLabel.setText('<span class="emph">CHB_exon.vcf</span> <span class="info">(server)</span>',false);
//
//	Ext.getCmp("Non-synonymous coding_"+this.id).setValue(true);
//	Ext.getCmp("Synonymous coding_"+this.id).setValue(true);
//	Ext.getCmp("Splice sites_"+this.id).setValue(true);
//	Ext.getCmp("Stop gained/lost_"+this.id).setValue(true);
//	Ext.getCmp("Upstream_"+this.id).setValue(true);
//	Ext.getCmp("Downstream_"+this.id).setValue(true);
//	Ext.getCmp("5' UTR_"+this.id).setValue(true);
//	Ext.getCmp("3' UTR_"+this.id).setValue(false);
//	Ext.getCmp("Non-coding RNA_"+this.id).setValue(true);
//	Ext.getCmp("Intergenic_"+this.id).setValue(false);
//
//	Ext.getCmp("Jaspar TFBS regions_"+this.id).setValue(true);
//	Ext.getCmp("miRNA targets_"+this.id).setValue(true);
//	Ext.getCmp("Other regulatory regions (CTCF, DNaseI, ...)_"+this.id).setValue(false);
//
//	Ext.getCmp("SNPs_"+this.id).setValue(true);
//	Ext.getCmp("Uniprot Natural Variants_"+this.id).setValue(false);
//
//	Ext.getCmp("Phenotypic annotated SNPs_"+this.id).setValue(false);
//	Ext.getCmp("Disease mutations_"+this.id).setValue(false);
//	Ext.getCmp(this.id+"speciesCombo").select(Ext.getCmp(this.id+"speciesCombo").findRecordByValue("hsa"));
//	console.log(this.paramsWS);
////	this.validateRunButton();

};
