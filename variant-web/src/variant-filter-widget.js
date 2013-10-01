function VariantFilterWidget(jobId, args) {
    var _this = this;
    this.id = "VariantFilterWidget_" + Math.round(Math.random() * 10000);
    this.jobId = jobId;

    this.targetId = null;

    this.title = null;
    this.width = 880;
    this.height = 325;


    this.args = args;

    if (args != null) {
        if (args.targetId != null) {
            this.targetId = args.targetId;
        }
        if (args.title != null) {
            this.title = args.title;
        }
        if (args.width != null) {
            this.width = args.width;
        }
        if (args.height != null) {
            this.height = args.height;
        }
        if (args.fileNames != null) {
            this.fileNames = args.fileNames;
        }
        if (args.viewer != null) {
            this.viewer = args.viewer;
        }
        if (args.tableLayout != null) {
            this.tableLayout = args.tableLayout;
        }
    }
    this.dataConsequence = new Array();
    this.resultTables = new Object();
    for (var fileName in this.fileNames) {


        this.resultTables[this.fileNames[fileName]] = new ResultTable(this.jobId, fileName, ["CONSEQUENCE_TYPE_VARIANTS"], {
            tableLayout: this.tableLayout,
            targetId: null,
            collapsible: false,
            numRows: 10,
            border: false,
            flex: 3,
            cls: 'ocb-border-left-lightgrey'
        });
        this.dataConsequence.push({name: this.fileNames[fileName]});
    }

    OpencgaManager.poll({
        accountId: $.cookie('bioinfo_account'),
        sessionId: $.cookie('bioinfo_sid'),
        jobId: _this.jobId,
        filename: "genes_with_variants.txt",
        zip: false,
        success: function (data) {
            var lines = data.split("\n");
            _this.dataGene = new Array();
            for (var i = 0; i < lines.length - 1; i++) {
                if (lines[i][0] != "#") {
                    _this.dataGene.push({name: lines[i]});
                }
                _this._stGene.loadData(_this.dataGene);
            }
            _this.getPanel(_this.targetId);
        }
    });
//	wumAdapter.onPoll.addEventListener();
//	wumAdapter.poll(this.jobId, "genes_with_variants.txt", false, $.cookie('bioinfo_sid'));

    //save genes to avoid the grep web service if has been already clicked
    this.genesData = new Object();
};

VariantFilterWidget.prototype.draw = function () {
    this._render();
    this._panel.show();
};

VariantFilterWidget.prototype.getPanel = function (component) {
    this._render("panel", component);
    return this._panel;

};

VariantFilterWidget.prototype._render = function (mode, targetId) {
    var _this = this;
    if (this._panel == null) {

        this._stConsequence = Ext.create('Ext.data.Store', {
            fields: ["name"],
            data: this.dataConsequence
        });

        var viewConsequence = Ext.create('Ext.view.View', {
            id: this.id + 'view',
            store: this._stConsequence,
            selModel: {
                mode: 'SINGLE',
//                allowDeselect:true,
                listeners: {
                    selectionchange: function (este, sel) {
                        if (sel.length > 0) {//sometimes returns empty
                            _this.optionSOClick(sel[0].data);
                        }
                    }
                }
            },
            listeners: {viewready: function (comp, eOpts) {
                var selModel = comp.getSelectionModel();
                if (selModel.getStore().getTotalCount() > 0) {
//                    selModel.selectRange(0, 0);
                }
            }},
            cls: 'list',
            trackOver: true,
            overItemCls: 'list-item-hover',
            itemSelector: '.list-item',
            tpl: '<tpl for="."><div class="list-item">{name}</div></tpl>'
        });

        var panConsequence = Ext.create('Ext.panel.Panel', {
            title: 'By Consequence type',
            id: this.id + "optionsPan",
            bodyPadding: 5,
            autoScroll: true,
            border: false,
            items: [viewConsequence]
        });

        this._stGene = Ext.create('Ext.data.Store', {
            fields: ["name"],
            data: this.dataGene
        });
        /**TEXT SEARCH FILTER**/
        var searchField = Ext.create('Ext.form.field.Text', {
            id: this.searchFieldId,
            flex: 1,
            emptyText: 'enter search term',
            enableKeyEvents: true,
            listeners: {
                change: function () {
                    var searchText = this.getValue().toLowerCase();
                    _this._stGene.clearFilter();
                    _this._stGene.filter(function (item) {
                        if (item.data.name.toLowerCase().indexOf(searchText) < 0) {
                            return false;
                        }
                        return true;
                    });
                }
            }
        });


        var viewGene = Ext.create('Ext.view.View', {
            id: this.id + 'viewGene',
            store: this._stGene,
            selModel: {
                mode: 'SINGLE',
//                allowDeselect:true,
                listeners: {
                    selectionchange: function (este, sel) {
                        if (sel.length > 0) {//sometimes returns empty
                            _this.optionGeneClick(sel[0].data);
                        }
                    }
                }
            },
            cls: 'list',
            trackOver: true,
            overItemCls: 'list-item-hover',
            itemSelector: '.list-item',
            tpl: '<tpl for="."><div class="list-item">{name}</div></tpl>'
        });

        var panGene = Ext.create('Ext.panel.Panel', {
            title: 'By Gene',
            border: false,
            bodyPadding: 5,
            autoScroll: true,
            items: [searchField, viewGene]
        });

        var panTab = Ext.create('Ext.tab.Panel', {
            minWidth: 240,
            width: 240,
            autoScroll: true,
            border: false,
            items: [panConsequence, panGene]
        });


        if (mode == 'panel') {
            this._panel = Ext.create('Ext.panel.Panel', {
                title: 'Variant filter tool',
                renderTo: targetId,
                height: this.height,
                width: this.width,
                collapsible: true,
                margin: '0 0 10 0',
                layout: { type: 'hbox', align: 'stretch'},
                items: [panTab]
            });
//            Ext.getCmp(targetId).insert(0, this._panel);
        } else {
            this._panel = Ext.create('Ext.ux.Window', {
                title: 'Variant filter tool',
//		    resizable: false,
                taskbar: Ext.getCmp(this.viewer.id + 'uxTaskbar'),
                minimizable: true,
                constrain: true,
                closable: true,
                height: this.height,
                width: this.width,
                layout: { type: 'hbox', align: 'stretch'},
                items: [panTab],
                listeners: {
                    minimize: function () {
                        _this._panel.hide();
                    },
                    close: function () {
                        delete _this._panel;
                    }
                }
            });
        }

        this._panel.on("afterrender", function () {
            viewConsequence.getSelectionModel().select(viewConsequence.store.first());
            panConsequence.on("activate", function () {
//				deselect and select the same on tab change
                var selModel = viewConsequence.getSelectionModel();
                var last = selModel.getLastSelected();
                if (last != null) {
                    selModel.deselect(last);
                    selModel.select(last);
                } else {
                    selModel.select(viewConsequence.store.first());
                }
            });
            panGene.on("activate", function () {
                //deselect and select the same on tab change
                var selModel = viewGene.getSelectionModel();
                var last = selModel.getLastSelected();
                if (last != null) {
                    selModel.deselect(last);
                    selModel.select(last);
                } else {
                    selModel.select(viewGene.store.first());
                }
            });
        });
    }
};


//VariantFilterWidget.prototype.parseData = function (data){
//	var _this=this;
//	//create a hash of arrays based on sequenceOntology
//	this._ontologyHash = {};
//	this._geneHash = {};
//	
//	var avoidSOList = ["regulatory_region_variant","intron_variant","INTERGENIC"];
//	
//	var lines = data.split("\n");
//	for (var i = 0; i < lines.length; i++){
//		var trimmed = lines[i].replace(/^\s+|\s+$/g,"");
//		trimmed = trimmed.replace(/\//gi,"");//TODO DONE   /  is not allowed in the call
//				
//		if ((trimmed != null)&&(trimmed.length > 0)){
//			var line = trimmed.replace(/\t/g,'**%**').split("**%**");
//			if (trimmed.substring(0,1) != "#"){
//				var soName = line[18];
//				var geneName = line[4];
//				//ontology
//				if(!Ext.Array.contains(avoidSOList,soName)){
//					if (this._ontologyHash[soName]==null){
//						this._ontologyHash[soName]=[];
//					}
//					this._ontologyHash[line[18]].push(line);
//				}
//				//gene
//				if(geneName!="."){
//					if (this._geneHash[geneName]==null){
//						this._geneHash[geneName]=[];
//					}
//					this._geneHash[line[4]].push(line);
//				}
//			}
//			
//		}
//	}//end for
//	//getting names
//	var ontologyNames = [];
//	for ( var key in this._ontologyHash) {
//		ontologyNames.push({name:key.replace(/_/gi, " ")});
//	}
//	
//	var geneNames = [];
//	for ( var key in this._geneHash) {
//		geneNames.push({name:key});
//	}
//	
//	
//	//load menu with ontologys found in the file
//	this._stConsequence.loadData(ontologyNames);
//	this._stGene.loadData(geneNames);
//	
//	//select the first record after load
//	
////	console.log(this._ontologyHash);
////	console.log(this._geneHash);
//};

VariantFilterWidget.prototype.optionSOClick = function (dataRecord) {
    var _this = this;
    var soName = dataRecord.name;
    if (this._panel.getComponent(1) != null) {
        this._panel.getComponent(1).hide();
        this._panel.remove(1, false);
    }
    this.resultTables[soName].draw();
    this._panel.add(this.resultTables[soName].table.show());

    //set location on Genome Viewer
    this.resultTables[soName].table.getSelectionModel().on("selectionchange", function (este, sel) {
        if (_this.args.viewer != null && sel.length > 0) {
            var r = {
                chromosome: sel[0].data.Chrom,
                start: parseInt(sel[0].data.Position) - 5000,
                end: parseInt(sel[0].data.Position) + 5000
            }
            _this.viewer.setRegion(r);
//            _this.viewer.onRegionChange.notify({sender: ""});
        }
    });
    this.resultTables[soName].table.getSelectionModel().getStore().on("load", function (store) {
        if (store.getTotalCount() > 0) {
            _this.resultTables[soName].table.getSelectionModel().selectRange(0, 0);
        }
    });
};

VariantFilterWidget.prototype.optionGeneClick = function (dataRecord) {
    var _this = this;
    var name = dataRecord.name;
    if (this._panel.getComponent(1) != null) {
        this._panel.getComponent(1).hide();
        this._panel.remove(1, false);
    }

    if (this[name + "Grid"] == null) {
//        var wumAdapter = new OpencgaManager();
        this._panel.setLoading();
//        wumAdapter.onGrep.addEventListener(function (sender, data) {
//            debugger
//            var storeData = new Array();
//            var lines = data.split("\n");
//            for (var i = 0; i < lines.length - 1; i++) {
//                if (lines[i][0] != "#") {
//                    var fields = lines[i].split("\t");
//                    storeData.push(fields);
//                    _this.genesData[name] = storeData;
//                }
//            }
//            _this._panel.add(_this.getByGeneGrid(_this.genesData[name], name).show());
//            _this._panel.setLoading(false);
//        });
//        wumAdapter.grep(this.jobId, "all_variants.txt", ".*" + name + ".*", true, $.cookie("bioinfo_sid"));

        OpencgaManager.poll({
            accountId: $.cookie('bioinfo_account'),
            sessionId: $.cookie('bioinfo_sid'),
            jobId: this.jobId,
            filename: "all_variants.txt",
            zip: false,
            success: function (data) {
                var storeData = [];
                var lines = data.split("\n");
                for (var i = 0; i < lines.length - 1; i++) {
                    var line = lines[i];
                    if (line[0] != "#" && line.indexOf(name) != -1) {
                        var fields = line.split("\t");
                        storeData.push(fields);
                        _this.genesData[name] = storeData;
                    }
                }
                _this._panel.add(_this.getByGeneGrid(_this.genesData[name], name).show());
                _this._panel.setLoading(false);
            }
        });

    } else {
        this._panel.add(this[name + "Grid"].show());
    }


};


//VariantFilterWidget.prototype.getBySOGrid = function(data, name){
//	var _this=this;
//	console.log(data[0]);
//    if(this[name+"Grid"]==null){
//    	var groupField = '';
//    	var modelName = name;
//    	var fields= ["chr", "start", "end", "EnsemblID", "Ext.Name", "FeatureType", "Biotype", "7","8","9","10","11","12","13","14","15","16","17","ConsequenceType"];
//    	var columns = [
//		    		  {"header":"chr","dataIndex":"chr",flex:0.5},
//		    		  {"header":"start","dataIndex":"start",flex:1.2},
//		    		  {"header":"end","dataIndex":"end",flex:1.2},
//		    		  {"header":"EnsemblID","dataIndex":"EnsemblID",flex:1.2},
//		    		  {"header":"Ext.Name","dataIndex":"Ext.Name",flex:1.2},
//		    		  {"header":"FeatureType","dataIndex":"FeatureType",flex:1.2},
//		    		  {"header":"Biotype","dataIndex":"Biotype",flex:1.3},
//		    		  {"header":"ConsequenceType","dataIndex":"ConsequenceType",flex:1}];    	
//		this[name+"Grid"] = this.doGrid(columns,fields,modelName,groupField);
//		this[name+"Grid"].store.loadData(data);
//		
//		this[name+"Grid"].getSelectionModel().on("selectionchange",function(este,sel){
//			if(_this.args.viewer!=null){
//				_this.args.viewer.setLocation(sel[0].data.chr,sel[0].data.start);
//			}
//		});
//    }
//    return this[name+"Grid"];
//};

VariantFilterWidget.prototype.getByGeneGrid = function (data, name) {
    var _this = this;
    if (this[name + "Grid"] == null) {
        var groupField = '';
        var modelName = name;
        var fields = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25"];
        var columns = [
            {"header": "Chrom", "dataIndex": "0", flex: 0.5},
            {"header": "Position", "dataIndex": "1", flex: 1},
            {"header": "Reference", "dataIndex": "2", flex: 1},
            {"header": "Alternative", "dataIndex": "3", flex: 1},
            {"header": "Feature ID", "dataIndex": "4", flex: 1.2},
            {"header": "Feature Type", "dataIndex": "6", flex: 1.1},
            {"header": "Biotype", "dataIndex": "7", flex: 1.2},
            {"header": "ConsequenceType", "dataIndex": "19", flex: 1.5},
            {"header": "SO acc", "dataIndex": "18", flex: 1}
        ];
        this[name + "Grid"] = this.doGrid(columns, fields, modelName, groupField, data);
        //this[name+"Grid"].store.loadData(data);

        //set location on Genome Viewer
        this[name + "Grid"].getSelectionModel().on("selectionchange", function (este, sel) {
            var r = {
                chromosome: sel[0].data[0],
                start: parseInt(sel[0].data[1]) - 5000,
                end: parseInt(sel[0].data[1]) + 5000
            }
            if (_this.args.viewer != null) {
                _this.viewer.setRegion(r);
            }
        });
    }
    return this[name + "Grid"];
};


VariantFilterWidget.prototype.doGrid = function (columns, fields, modelName, groupField, data) {
    var groupFeature = Ext.create('Ext.grid.feature.Grouping', {
        groupHeaderTpl: groupField + ' ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
    });
    var filters = [];
    for (var i = 0; i < fields.length; i++) {
        filters.push({type: 'string', dataIndex: fields[i]});
    }
    var filters = {
        ftype: 'filters',
        local: true, // defaults to false (remote filtering)
        filters: filters
    };
    Ext.define(modelName, {
        extend: 'Ext.data.Model',
        fields: fields
    });
//    var store = Ext.create('Ext.data.Store', {
//        groupField: groupField,
////		model:modelName
//        data: data,
//        fields: fields,
//        proxy: {type: 'pagingmemory'}, pageSize: 10
//    });
    var store = Ext.create('Ext.data.Store', {
        groupField: groupField,
        //		model:modelName
        data: data,
        fields: fields,
        proxy: {type: 'memory'}, pageSize: 10
    });

    var grid = Ext.create('Ext.grid.Panel', {
        id: this.id + modelName,
        store: store,
        title: modelName,
        border: false,
        cls: 'ocb-border-left-lightgrey',
        flex: 3,
//        dockedItems: [
//            {xtype: 'pagingtoolbar', store: store, dock: 'top', displayInfo: true,
//                items: ['-', { text: 'Clear Grouping',
//                    handler: function () {
//                        groupFeature.disable();
//                    }}]
//            }
//        ],
        featuresSvgNode: [groupFeature, filters],
        columns: columns,
        plugins: 'bufferedrenderer', loadMask: true,
        viewConfig: {
//            stripeRows: true,
            enableTextSelection: true
        }
    });
    return grid;
};

