function CxVariantFilterWidget(args){
	var _this=this;
	this.id = "CxVariantFilterWidget_";// + Math.round(Math.random()*10000);
	this.targetId = null;

	this.title = null;
   	this.width = 880;
	this.height = 420;
	
	this.args = args;
	
	if (args != null){
        if (args.targetId!= null){
        	this.targetId = args.targetId;       
        }
        if (args.title!= null){
        	this.title = args.title;       
        }
        if (args.width!= null){
        	this.width = args.width;       
        }
        if (args.height!= null){
        	this.height = args.height;       
        }
    }
	
};

CxVariantFilterWidget.prototype.draw = function (){
	this._render();
	this._panel.show();
};

CxVariantFilterWidget.prototype.getPanel = function (div){
	this._render("panel",div);
	return this._panel;
};

CxVariantFilterWidget.prototype._render = function (mode,targetId){
	var _this=this;
	if(this._panel==null){
		
		this._stConsequence = Ext.create('Ext.data.Store', {
	 		fields: ["name"],
	 		data : []
		});
		
		var viewConsequence = Ext.create('Ext.view.View', {
			id:this.id+'view',
		    store : this._stConsequence,
            selModel: {
                mode: 'SINGLE',
//                allowDeselect:true,
                listeners: {
                	selectionchange:function(este,sel){
                    	if(sel.length>0){//sometimes returns empty
                    		_this.optionSOClick(sel[0].data);
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
         
        var panConsequence = Ext.create('Ext.panel.Panel', {
        	title:'By Consequence type',
        	id:this.id+"optionsPan",
			bodyPadding:5,
			autoScroll:true,
		    border:false,
		    items : [viewConsequence]
		});
        
        this._stGene = Ext.create('Ext.data.Store', {
        	fields: ["name"],
        	data : []
        });
		/**TEXT SEARCH FILTER**/
        var searchField = Ext.create('Ext.form.field.Text',{
        	 id:this.searchFieldId,
	         flex:1,
			 emptyText: 'enter search term',
			 enableKeyEvents:true,
			 listeners:{
			 	change:function(){
			 		var searchText = this.getValue().toLowerCase();
			 		_this._stGene.clearFilter();
			 		_this._stGene.filter(function(item){
						if(item.data.name.toLowerCase().indexOf(searchText)<0){
							return false;
						}
						return true;
					});
			 	}
			 }
        });
        
        
		var viewGene = Ext.create('Ext.view.View', {
			id:this.id+'viewGene',
		    store : this._stGene,
            selModel: {
                mode: 'SINGLE',
//                allowDeselect:true,
                listeners: {
                    selectionchange:function(este,sel){_this.optionGeneClick(sel[0].data);}
                }
            },
            cls: 'list',
         	trackOver: true,
            overItemCls: 'list-item-hover',
            itemSelector: '.list-item',
            tpl: '<tpl for="."><div class="list-item">{name}</div></tpl>'
        });
        
        var panGene = Ext.create('Ext.panel.Panel', {
        	title:'By Gene',
		    border:false,
			bodyPadding:5,
			autoScroll:true,
		    items:[searchField,viewGene]
		});
        
        var panTab = Ext.create('Ext.tab.Panel', {
			minWidth: 240,
			width:240,
			autoScroll:true,
		    border:false,
		    items : [panConsequence,panGene]
		});
        
		
		if(mode=='panel'){
			this._panel = Ext.create('Ext.panel.Panel', {
				title: 'Variant filter tool',
				renderTo:targetId,
				height:this.height,
				width:this.width,
				layout: { type: 'hbox',align: 'stretch'},
				items: [panTab]
			});
		}else{
			this._panel = Ext.create('Ext.ux.Window', {
				title: 'Variant filter tool',
//		    resizable: false,
				taskbar:Ext.getCmp(this.args.genomeViewer.id+'uxTaskbar'),
				minimizable :true,
				constrain:true,
				closable:true,
				height:this.height,
				width:this.width,
				layout: { type: 'hbox',align: 'stretch'},
				items: [panTab],
				listeners: {
					 minimize:function(){
						 _this._panel.hide();
					 },
					 destroy: function(){
						 delete _this._panel;
					 }
				}
			});
		}
	}
};


CxVariantFilterWidget.prototype.parseData = function (data){
	var _this=this;
	//create a hash of arrays based on sequenceOntology
	this._ontologyHash = {};
	this._geneHash = {};
	
	var avoidSOList = ["regulatory_region_variant","intron_variant","INTERGENIC"];
	
	var lines = data.split("\n");
	for (var i = 0; i < lines.length; i++){
		var trimmed = lines[i].replace(/^\s+|\s+$/g,"");
		trimmed = trimmed.replace(/\//gi,"");//TODO DONE   /  is not allowed in the call
				
		if ((trimmed != null)&&(trimmed.length > 0)){
			var line = trimmed.replace(/\t/g,'**%**').split("**%**");
			if (trimmed.substring(0,1) != "#"){
				var soName = line[19];
				var geneName = line[5];
				//ontology
				if(!Ext.Array.contains(avoidSOList,soName)){
					if (this._ontologyHash[soName]==null){
						this._ontologyHash[soName]=[];
					}
					this._ontologyHash[line[19]].push(line);
				}
				//gene
				if(geneName!="."){
					if (this._geneHash[geneName]==null){
						this._geneHash[geneName]=[];
					}
					this._geneHash[line[5]].push(line);
				}
			}
			
		}
	}//end for
	//getting names
	var ontologyNames = [];
	for ( var key in this._ontologyHash) {
		ontologyNames.push({name:key.replace(/_/gi, " ")});
	}
	
	var geneNames = [];
	for ( var key in this._geneHash) {
		geneNames.push({name:key});
	}
	
	
	//load menu with ontologys found in the file
	this._stConsequence.loadData(ontologyNames);
	this._stGene.loadData(geneNames);
//	console.log(this._ontologyHash);
//	console.log(this._geneHash);
	
	if(ontologyNames.length > 0){
		Ext.getCmp("CxVariantFilterWidget_view").getSelectionModel().select(0);
	}
};

CxVariantFilterWidget.prototype.optionSOClick = function (dataRecord){
//	console.log(dataRecord);
	if(dataRecord!=null){
		var soName = dataRecord.name;
		if(this._panel.getComponent(1)!=null){
			this._panel.getComponent(1).hide();
			this._panel.remove(1,false);
		}
		this._panel.add(this.getByGeneGrid(this._ontologyHash[soName.replace(/ /gi, "_")],soName).show());
	}
};

CxVariantFilterWidget.prototype.optionGeneClick = function (dataRecord){
//	console.log(dataRecord);
	if(dataRecord!=null){//some times the view selModel returns undefined if you click fast
		var geneName = dataRecord.name;
		if(this._panel.getComponent(1)!=null){
			this._panel.getComponent(1).hide();
			this._panel.remove(1,false);
		}
		this._panel.add(this.getByGeneGrid(this._geneHash[geneName],geneName).show());
	}
};


CxVariantFilterWidget.prototype.getByGeneGrid = function(data, name){
	var _this=this;
    if(this[name+"Grid"]==null){
    	var groupField = '';
    	var modelName = name;
    	var fields= ["Chrom", "Position", "Reference", "Alternative", "Feature ID", "Ext Name", "Feature Type", "Biotype","8","9","10","11","12","13","14","15","16","17","SO","Consequence Type","20","21","22","23","24","25"];
    	var columns = [
		    		  {"header":"Chrom","dataIndex":"Chrom",flex:0.5},
		    		  {"header":"Position","dataIndex":"Position",flex:1},
		    		  {"header":"Reference","dataIndex":"Reference",flex:1},
		    		  {"header":"Alternative","dataIndex":"Alternative",flex:1},
		    		  {"header":"Feature ID","dataIndex":"Feature ID",flex:1.2},
		    		  {"header":"Feature Type","dataIndex":"Feature Type",flex:1.1},
		    		  {"header":"Biotype","dataIndex":"Biotype",flex:1.2},
		    		  {"header":"ConsequenceType","dataIndex":"Consequence Type",flex:1.5},
		    		  {"header":"SO acc","dataIndex":"SO",flex:1}];    	
		this[name+"Grid"] = this.doGrid(columns,fields,modelName,groupField);
		this[name+"Grid"].store.loadData(data);
    }
    return this[name+"Grid"];
};


CxVariantFilterWidget.prototype.doGrid = function (columns,fields,modelName,groupField){
	var groupFeature = Ext.create('Ext.grid.feature.Grouping',{
		groupHeaderTpl: groupField+' ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
    });
	var filters = [];
	for(var i=0; i<fields.length; i++){
		filters.push({type:'string', dataIndex:fields[i]});
	}
	var filters = {
			ftype: 'filters',
			local: true, // defaults to false (remote filtering)
			filters: filters
	};
    Ext.define(modelName, {
	    extend: 'Ext.data.Model',
    	fields:fields
	});
   	var store = Ext.create('Ext.data.Store', {
		groupField: groupField,
		model:modelName
    });
	var grid = Ext.create('Ext.grid.Panel', {
		id: this.id+modelName,
        store: store,
        title : modelName,
        border:false,
        cls:'panel-border-left',
		flex:3,        
        featuresSvgNode: [groupFeature,filters],
        columns: columns,
        bbar  : ['->', {
            text:'Clear Grouping',
            handler : function(){
                groupFeature.disable();
            }
        }]
    });
return grid;
};

