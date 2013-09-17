function VariantWidget(args) {
    var _this = this;
    _.extend(this, Backbone.Events);

    this.id = Utils.genId("VariantWidget");

    //set default args
    this.border = true;
    this.autoRender = false;
    this.targetId;
    this.width;
    this.height;


    //set instantiation args, must be last
    _.extend(this, args);


    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
}

VariantWidget.prototype = {
    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        console.log("Initializing Variant Widget");
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="' + this.id + 'VariantWidget" style="height:100%;position:relative;"></div>')[0];
        $(this.targetDiv).append(this.div);


    },
    draw: function () {
        var _this = this;

        /* main panel */
        this.panel = this._createPanel($(this.div).attr('id'));


        this.optValues = Ext.create('Ext.data.Store', {
            fields: ['value', 'name'],
            data: [
                {"value": "<", "name": "<"},
                {"value": "<=", "name": "<="},
                {"value": ">", "name": ">"},
                {"value": ">=", "name": ">="},
                {"value": "=", "name": "="},
                {"value": "!=", "name": "!="}
            ],
            pageSize: 20
        });

        /* form */

        this.form = this._createForm();
        this.grid = this._createGrid();
        this.gridEffect = this._createEffectGrid();
        var _stEffect = this.stEffect;


        this.panel.insert(0, this.form);
        //        this.panel.add(this.grid);

        Ext.getCmp(this.id + 'rightpanel').add(this.grid);

        Ext.getCmp(this.id + 'rightpanel').add(this.gridEffect);

        this.grid.getSelectionModel().on('selectionchange', function (sm, selectedRecord) {

            if (selectedRecord.length) {

                var row = selectedRecord[0].data;
                var chr = row.chromosome;
                var pos = row.position;
                var ref = row.ref;
                var alt = row.alt;

                var db_name = _this.form.getForm().getValues()['db_name'];

                var formParams = {}

                formParams['db_name'] = db_name;
                formParams['chr'] = chr;
                formParams['pos'] = pos;
                formParams['ref'] = ref;
                formParams['alt'] = alt;

                var url = "http://aaleman:8080/bierapp/rest/effect";
                console.log(url);


                _this.gridEffect.setLoading(true);

                $.ajax({
                    type: "POST",
                    url: url,
                    data: formParams,
                    dataType: 'json',
                    success: function (response, textStatus, jqXHR) {
                        console.log(response)

                        if (response.length > 0) {

//                            _stEffect.removeAll();

//                            _this.stEffect.loadData(response);
                            _this.gridEffect.getStore().loadData(response);


                        } else {
                            _this.gridEffect.getStore().removeAll();
                        }
                        _this.gridEffect.setLoading(false);


                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log('Error loading Effect');
                        _this.gridEffect.setLoading(false);

                    }
                });


            }
        });

    },
    _createPanel: function (targetId) {

        var panel = Ext.create('Ext.panel.Panel', {
            title: 'Variant Widget',
            renderTo: targetId,
            width: '100%',
            height: '100%',
            border: 0,
            layout: 'hbox',
            cls: 'ocb-border-top-lightgrey',
            items: [
                {
                    id: this.id + 'rightpanel',
                    flex: 4,
                    height: '98%',
                    xtype: 'panel',
                    layout: 'vbox'

                }
            ]
        });
        return panel;
    },
    _createForm: function () {
        var _this = this;

        var accordion = Ext.create('Ext.form.Panel',{
            //title: 'FORM',
            border: false,
            flex: 1,
            height: "100%",
            title:"Filters",
            width:"100%",
            layout:{
                type: 'accordion',
            fill:false
            }

        });

        var regionItems = [
            this._getSelectDataPanel(),
            this._getRegionList(),
            this._getGenes(),
            this._getBioTypes()
            ];

        var region = Ext.create('Ext.panel.Panel',{
            title:"Region",
            items: regionItems
        
        });

        var statsItems = [
            this._getMAF(),
            this._getMissing(),
            this._getMendelError(),
            this._getIsIndel(),
            this._getInheritance()
        ];

        var stats = Ext.create('Ext.panel.Panel',{
            title:"Stats",
            items: statsItems
        
        });
        
        var controlsItems = [
            this._getControls()
        ];

        var controls = Ext.create('Ext.panel.Panel',{
            title:"Controls",
            items: controlsItems
        
        });

        var effectItems = [
            this._getConsequenceType()
            ];

        var effect = Ext.create('Ext.panel.Panel',{
            title:"Effect",
            items: effectItems
        
        });

        var searchItems =[
            {
                xtype: 'button',
                text: 'Search',
                padding: 10,
                handler: function () {
                    _this._getResult();
                }
            }
            ];

        var search = Ext.create('Ext.panel.Panel',{
            title:"Search",
            items: searchItems
        
        });

        //var form = Ext.create('Ext.form.Panel', {
            ////title: 'FORM',
            //border: false,
            //// bodyPadding: '5',
            //layout: 'vbox',
            //flex: 1,
            //height: "100%",
////            overflowY: 'scroll',
        //});

       accordion.add(region);
       accordion.add(stats);
       accordion.add(controls);
       accordion.add(effect);
       accordion.add(search);

        return accordion;
    },

    _createEffectGrid: function () {

        var groupingFeature = Ext.create('Ext.grid.feature.Grouping', {
            groupHeaderTpl: '{groupField}: {groupValue} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
        });
        this.stEffect = Ext.create("Ext.data.Store", {
            groupField: 'featureId',
            fields: [
                {name: "featureId", type: "String"},
                {name: "featureName", type: "String"},
                {name: "featureType", type: "String"},
                {name: "featureBiotype", type: "String"},
                {name: "featureChromosome", type: "String"},
                {name: "featureStart", type: "int"},
                {name: "featureEnd", type: "int"},
                {name: "featureStrand", type: "String"},
                {name: "snpId", type: "String"},
                {name: "ancestral", type: "String"},
                {name: "alternative", type: "String"},
                {name: "geneId", type: "String"},
                {name: "transcriptId", type: "String"},
                {name: "geneName", type: "String"},
                {name: "consequenceType", type: "String"},
                {name: "consequenceTypeObo", type: "String"},
                {name: "consequenceTypeDesc", type: "String"},
                {name: "consequenceTypeType", type: "String"},
                {name: "aaPosition", type: "int"},
                {name: "aminoacidChange", type: "String"},
                {name: "codonChange", type: "String"}
            ],
            data: [],
            autoLoad: false,
            proxy: {type: 'memory'},
            pageSize: 5
        });

        var gridEffect = Ext.create('Ext.grid.Panel', {
            flex: 1,
            width: '100%',
            store: this.stEffect,
            loadMask: true,
            border: 0,
            titleCollapse: true,
            columns: [
                {xtype: 'rownumberer'},
                {
                    text: "Position chr:start:end (strand)",
                    dataIndex: "featureChromosome",
                    xtype: "templatecolumn",
                    tpl: "{featureChromosome}:{featureStart}-{featureEnd} ({featureStrand})",
                    flex: 1
                },
                {
                    text: "snp Id",
                    dataIndex: "snpId",
                    flex: 1
                },
                {
                    text: "Consequence Type",
                    dataIndex: "consequenceType",
                    xtype: "templatecolumn",
                    tpl: "{consequenceTypeObo} ({consequenceType})",
                    flex: 1
                },
                {
                    text: "Aminoacid Change",
                    xtype: "templatecolumn",
                    tpl: "{aminoacidChange} - {codonChange} ({aaPosition})",
                    flex: 1
                },
                {
                    text: "gene (EnsemblId)",
                    dataIndex: "geneName",
                    xtype: 'templatecolumn',
                    tpl: '{geneName} (<a href="http://www.ensembl.org/Homo_sapiens/Location/View?g={geneId}" target="_blank">{geneId}</a>)',
                    flex: 1
                },
                {
                    text: "transcript Id",
                    dataIndex: "transcriptId",
                    xtype: 'templatecolumn',
                    tpl: '<a href="http://www.ensembl.org/Homo_sapiens/Location/View?t={transcriptId}" target="_blank">{transcriptId}</a>',
                    flex: 1
                },
                {
                    text: "feature Id",
                    dataIndex: "featureId",
                    flex: 1

                },
                {
                    text: "feature Name",
                    dataIndex: "featureName",
                    flex: 1

                },
                {
                    text: "feature Type",
                    dataIndex: "featureType",
                    flex: 1

                },
                {
                    text: "feature Biotype",
                    dataIndex: "featureBiotype",
                    flex: 1

                },
                {
                    text: "ancestral",
                    dataIndex: "ancestral",
                    hidden: true,
                    flex: 1
                },
                {
                    text: "alternative",
                    dataIndex: "alternative",
                    hidden: true,
                    flex: 1
                }
            ],
            features: [groupingFeature]
        });
        return gridEffect
    },

    _createGrid: function () {


        this.st = Ext.create('Ext.data.Store', {
//            groupField: 'stats_id_snp',
            fields: [
                {name: "chromosome", type: "String"},
                {name: "position", type: "int"},
                {name: "alt", type: "String"},
                {name: "ref", type: "String"},
                {name: 'stats_id_snp', type: 'string'},
                {name: 'stats_maf', type: 'float'},
                {name: 'stats_mgf', type: 'double'},
                {name: 'stats_allele_maf', type: 'string'},
                {name: 'stats_genotype_maf', type: 'string'},
                {name: 'stats_miss_allele', type: 'int'},
                {name: 'stats_miss_gt', type: 'int'},
                {name: 'stats_mendel_err', type: 'int'},
                {name: 'stats_is_indel', type: 'boolean'},
                {name: 'stats_cases_percent_dominant', type: 'double'},
                {name: 'stats_controls_percent_dominant', type: 'double'},
                {name: 'stats_cases_percent_recessive', type: 'double'},
                {name: 'stats_controls_percent_recessive', type: 'double'},
                {name: "genotypes", type: 'auto'},
                {name: "effect", type: 'auto'},
                {name: "controls", type: 'auto'}

            ],
            data: [],
            autoLoad: false,
            proxy: {type: 'memory'},
            pageSize: 5

        });
        var groupingFeature = Ext.create('Ext.grid.feature.Grouping', {
            groupHeaderTpl: '{groupField}: {groupValue} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
        });

        var grid = Ext.create('Ext.grid.Panel', {

            flex: 1,
            width: '100%',
            store: this.st,
            loadMask: true,
            border: 0,
            titleCollapse: true,
//            features: [groupingFeature],
            columns: [
                {xtype: 'rownumberer'},
                {
                    text: "Variant Pos",
                    dataIndex: 'chromosome',
                    flex: 1.5,
                    renderer: function (val, meta, record) {
                        return record.data.chromosome + ":" + record.data.position;
                    }
                },
                {
                    text: "ID",
                    dataIndex: 'stats_id_snp.',
                    flex: 1,
                    sortable: true

                },
                {
                    text: "Alleles & Genotypes",
                    columns: [
                        {

                            text: "Allele Ref",
                            dataIndex: 'ref',
                            flex: 0.2,
                            sortable: true
                        },
                        {
                            text: "Allele Alt",
                            dataIndex: 'alt',
                            flex: 0.2,
                            sortable: true
                        },

                        {
                            header: "MAF",
                            dataIndex: 'stats_maf',
                            xtype: "templatecolumn",
                            tpl: "{stats_maf} ({stats_allele_maf})",
                            flex: 0.2,
                            sortable: true
                        },
                        {
                            text: "MGF",
                            dataIndex: 'stats_mgf',
                            xtype: "templatecolumn",
                            tpl: "{stats_mgf} ({stats_genotype_maf})",
                            flex: 0.2,
                            sortable: true
                        }
                    ]
                },

                {
                    text: "Missing Alleles/Genotypes",
                    columns: [
                        {
                            text: "Alleles",
                            dataIndex: 'stats_miss_allele',
                            flex: 0.1,
                            sortable: true
                        },
                        {
                            text: "Genotypes",
                            dataIndex: 'stats_miss_gt',
                            flex: 0.1,
                            sortable: true
                        }
                    ]
                },
                {
                    flex: 1,
                    text: "Mendelian Errors",
                    dataIndex: 'stats_mendel_err',
                    sortable: true
                },
                {
                    flex: 1,
                    text: "Is indel?",
                    xtype: 'booleancolumn',
                    trueText: 'Yes',
                    falseText: 'No',
                    dataIndex: 'stats_is_indel',
                    sortable: true
                },
                {
                    flex: 1,
                    text: "Inheritance",
                    columns: [
                        {
                            text: "% Cases dominant",
                            dataIndex: 'stats_cases_percent_dominant',
                            renderer: function (value) {
                                return value.toFixed(2);
                            },
                            sortable: true
                        },
                        {
                            text: "% Controls dominant",
                            dataIndex: 'stats_controls_percent_dominant',
                            renderer: function (value) {
                                return value.toFixed(2) + "%";
                            },
                            sortable: true
                        },
                        {
                            text: "% Cases recessive",
                            dataIndex: 'stats_cases_percent_recessive',
                            renderer: function (value) {
                                return value.toFixed(2) + "%";
                            },
                            sortable: true
                        },
                        {
                            text: "% Controls recessive",
                            dataIndex: 'stats_controls_percent_recessive',
                            renderer: function (value) {
                                return value.toFixed(2) + "%";
                            },
                            sortable: true
                        }
                    ]
                },
                {
                    flex: 1,
                    text: "Controls (MAF)",
                    columns: [
                        {
                            text: "1000G",
                            renderer: function (val, meta, record) {
                                if (record.data.controls["1000G"]) {

                                    return record.data.controls["1000G"].maf + " (" + record.data.controls["1000G"].allele + ")";
                                } else {
                                    return ".";
                                }

                            }
                        }


                    ]
                }
            ],
            plugins: 'bufferedrenderer', loadMask: true
        });
        return grid;
    },
    _getResult: function () {
        var _this = this;
        var values = this.form.getForm().getValues();
        var formParams = {}
        for (var param in values) {
            formParams[param] = values[param];
        }
        var url = "http://aaleman:8080/bierapp/rest/post";
        console.log(url);
        _this.grid.setLoading(true);
        $.ajax({
            type: "POST",
            url: url,
            data: formParams,
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                console.log(response)
                _this.st.loadData(response);


                //                console.log(_this.grid.columns.length)
                if (response.length > 0 && !_this.firstTime) {

                    var sample_cols = [];

                    _this.firstTime = true;
                    samples_names = []

                    samples_genotypes = response[0].genotypes;
                    for (var key in samples_genotypes) {
                        //                        console.log(key);
                        var col = Ext.create("Ext.grid.column.Column", {
                            header: key,
                            sortable: true,
                            flex: 0.1,
                            //                            flex: 1,
                            renderer: function (val, meta, record) {
                                var val = record.data.genotypes[meta.column.text];
                                return val.replace(/-1/g, ".");
                            }
                        });
                        sample_cols.push(col);
                    }
                    var sample_col = Ext.create("Ext.grid.column.Column", {
                        header: "Samples",
                        columns: sample_cols
                    });

                    _this.grid.headerCt.insert(_this.grid.columns.length, sample_col);
                    //_this.grid.reconfigure(store, columns[])


                }
                _this.grid.getView().refresh();
                _this.grid.setLoading(false);

            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log('no va');
                _this.grid.setLoading(false);
            }
        });

    },

    ////
    ////
    /*FORM COMPONENTS*/
    ////
    ////

    _getSelectDataPanel: function () {

        var dataBases = Ext.create('Ext.data.Store', {
            fields: ['value', 'name'],
            data: [
                {"value": "s4.db", "name": "s4"},
                {"value": "s5500.db", "name": "s5500"}
            ]
        });

        var data_opt = this._createCombobox("db_name", "Data Base", dataBases, 0, 100, '5 0 5 5');

        return Ext.create('Ext.form.Panel', {
//            title: 'Data',
            //border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            buttonAlign: 'center',
            layout: 'vbox',
            items: [data_opt]
        });
    },
    _getRegionList: function () {
        var position = Ext.create('Ext.form.field.TextArea', {
            id: "chrpos",
            name: "chrpos",
            fieldLabel: 'Chromosomic position',
            margin: '0 0 0 5',
            value: "1:1-1000000",
            allowBlank: false
        });

        return Ext.create('Ext.form.Panel', {
//            title: 'Chromosomic position',
            border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            buttonAlign: 'center',
            layout: 'vbox',
            items: [position]
        });
    },
    _getGenes: function () {
        var geneList = Ext.create('Ext.form.field.TextArea', {
            id: "genes",
            name: "genes",
            fieldLabel: 'Gene list ',
            margin: '0 0 0 5',
            allowBlank: false
        });

        return Ext.create('Ext.form.Panel', {
            border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            buttonAlign: 'center',
            layout: 'vbox',
            items: [geneList]
        });
    },
    _getBioTypes: function () {
        var bt = Ext.create('Ext.form.field.TextArea', {
            id: "biotype",
            name: "biotype",
            fieldLabel: 'Biotypes',
            margin: '0 0 0 5',
            allowBlank: false
        });

        return Ext.create('Ext.form.Panel', {
            border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            buttonAlign: 'center',
            layout: 'vbox',
            items: [bt]
        });
    },
    _getConsequenceType: function () {
        var ct = Ext.create('Ext.form.field.Text', {
            id: "conseq_type",
            name: "conseq_type",
            fieldLabel: 'Consequence Type',
            margin: '0 0 0 5',
            allowBlank: false
        });

        return Ext.create('Ext.form.Panel', {
            border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            buttonAlign: 'center',
            layout: 'vbox',
            items: [ct]
        });
    },
    _getMissing: function () {
        var alleles_text = Ext.create('Ext.form.field.Text', {
            id: "miss_allele",
            name: "miss_allele",
            margin: '0 0 0 5',
            width: "20%",
            allowBlank: false
        });

        var alleles_opt = this._createCombobox("option_miss_alleles", "", this.optValues, 0, 10, '0 0 0 5');
        alleles_opt.width = "20%";

        var gt_text = Ext.create('Ext.form.field.Text', {
            id: "miss_gt",
            name: "miss_gt",
            margin: '0 0 0 5',
            allowBlank: false,
            width: "20%"
        });

        var gt_opt = this._createCombobox("option_miss_gt", "", this.optValues, 0, 10, '0 0 0 5');
        gt_opt.width = "20%";

        return Ext.create('Ext.form.Panel', {
//            title: 'Missing Alleles & Genotypes',
            border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            type: 'vbox',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Missing Alleles',
                    layout: 'hbox',
                    border: false,
                    items: [alleles_text, alleles_opt] },

                {
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Missing Genotypes',
                    layout: 'hbox',
                    border: false,
                    items: [gt_text, gt_opt]}
            ]
        });
    },
    _getMAF: function () {
        var maf_text = Ext.create('Ext.form.field.Text', {
            id: "maf",
            name: "maf",
            margin: '0 0 0 5',
            width: "20%",
            allowBlank: false
        });

        var maf_opt = this._createCombobox("option_maf", "", this.optValues, 0, 10, '0 0 0 5');
        maf_opt.width = "20%";

        var mgf_text = Ext.create('Ext.form.field.Text', {
            id: "mgf",
            name: "mgf",
            margin: '0 0 0 5',
            allowBlank: false,
            width: "20%"
        });

        var mgf_opt = this._createCombobox("option_mgf", "", this.optValues, 0, 10, '0 0 0 5');
        mgf_opt.width = "20%";

        return Ext.create('Ext.form.Panel', {
//            title: 'MAF & MGF',
            border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            type: 'vbox',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: 'MAF',
                    layout: 'hbox',
                    border: false,
                    items: [maf_text, maf_opt] },

                {
                    xtype: 'fieldcontainer',
                    fieldLabel: 'MGF',
                    layout: 'hbox',
                    border: false,
                    items: [mgf_text, mgf_opt]}
            ]
        });
    },
    _getMendelError: function () {
        var mendel_text = Ext.create('Ext.form.field.Text', {
            id: "mend_error",
            name: "mend_error",
            margin: '0 0 0 5',
            width: "20%",
            allowBlank: false
        });

        var mendel_opt = this._createCombobox("option_mend_error", "", this.optValues, 0, 10, '0 0 0 5');
        mendel_opt.width = "20%";

        return Ext.create('Ext.form.Panel', {
//            title: 'Mendelian Errors',
            //border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            buttonAlign: 'center',
            type: 'vbox',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: 'Mendel. Errors',
                    layout: 'hbox',
                    border: false,
                    items: [mendel_text, mendel_opt]
                }
            ]

        });
    },
    _getIsIndel: function () {
        return Ext.create('Ext.form.Panel', {
//            title: 'Is indel?',
            //border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "99%",
            buttonAlign: 'center',
            layout: 'vbox',
            items: [
                {
                    xtype: 'checkboxfield',
                    fieldLabel: 'Is indel?',
                    //anchor: '100%',
                    name: 'is_indel'
                    //checked: true

                }
            ]
        });
    },
    _getInheritance: function () {

        var cases_d = Ext.create('Ext.form.field.Text', {
            id: "cases_percent_dominant",
            name: "cases_percent_dominant",
            margin: '0 0 0 5',
            width: "20%",
            allowBlank: false
        });

        var cases_d_opt = this._createCombobox("option_cases_dom", "", this.optValues, 0, 10, '0 0 0 5');
        cases_d_opt.width = "20%";

        var controls_d = Ext.create('Ext.form.field.Text', {
            id: "controls_percent_dominant",
            name: "controls_percent_dominant",
            margin: '0 0 0 5',
            width: "20%",
            allowBlank: false
        });

        var controls_d_opt = this._createCombobox("option_controls_dom", "", this.optValues, 0, 10, '0 0 0 5');
        controls_d_opt.width = "20%";

        var cases_r = Ext.create('Ext.form.field.Text', {
            id: "cases_percent_recessive",
            name: "cases_percent_recessive",
            margin: '0 0 0 5',
            width: "20%",
            allowBlank: false
        });

        var cases_r_opt = this._createCombobox("option_cases_rec", "", this.optValues, 0, 10, '0 0 0 5');
        cases_r_opt.width = "20%";

        var controls_r = Ext.create('Ext.form.field.Text', {
            id: "controls_percent_recessive",
            name: "controls_percent_recessive",
            margin: '0 0 0 5',
            width: "20%",
            allowBlank: false
        });

        var controls_r_opt = this._createCombobox("option_controls_rec", "", this.optValues, 0, 10, '0 0 0 5');
        controls_r_opt.width = "20%";

        return Ext.create('Ext.form.Panel', {
//            title: 'Inheritance',
            border: true,
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "100%",
            buttonAlign: 'center',
            type: 'vbox',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '% Cases Dominant',
                    layout: 'hbox',
                    border: false,
                    items: [cases_d, cases_d_opt]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '% Controls Dominant',
                    layout: 'hbox',
                    border: false,
                    items: [controls_d, controls_d_opt]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '% Cases recessive',
                    layout: 'hbox',
                    border: false,
                    items: [cases_r, cases_r_opt]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '% Controls recessive',
                    layout: 'hbox',
                    border: false,
                    items: [controls_r, controls_r_opt]
                }
            ]
        });


    },
    _getControls: function () {
        return Ext.create('Ext.form.Panel', {
            bodyPadding: "5",
            margin: "0 0 5 0",
            width: "99%",
            buttonAlign: 'center',
            layout: 'vbox',
            items: [
                {
                    xtype: 'checkboxfield',
                    fieldLabel: 'Exclude 1000G Controls',
                    //anchor: '100%',
                    name: 'exc_1000g_controls'
//                    checked: true

                }
            ]
        });
    },
    _createCombobox: function (name, label, data, defaultValue, labelWidth, margin) {
        return Ext.create('Ext.form.field.ComboBox', {
            id: name,
            name: name,
            fieldLabel: label,
            store: data,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'value',
            value: data.getAt(defaultValue).get('value'),
            labelWidth: labelWidth,
            margin: margin,
            editable: false,
            allowBlank: false
        });
    }
}
