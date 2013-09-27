function Variant (args){

    _.extend(this, Backbone.Events);

    var _this = this;
    this.id = Utils.genId("Variant");

	this.suiteId = 6;
	this.tools = ["hpg-variant.effect"];
	this.title = '<span class="emph">Vari</span>ant <span class="emph">an</span>alysis <span class="emph">t</span>ool';
    this.description = '';
    this.version = '1.1.0';

    this.border= true;
	
	this.width =  $(window).width();
	this.height = $(window).height();
	this.targetId=document.body;



	//RESIZE EVENT
	$(window).resize(function(a){
		_this.setSize($(window).width(),$(window).height());
	});


    _.extend(this, args);

    this.accountData = null;

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
	
}


Variant.prototype = {
    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }
        console.log("Initializing Variant");
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="genome-maps"></div>')[0];
        $(this.targetDiv).append(this.div);

        $(this.div).append('<div id="va-header-widget"></div>');
        $(this.div).append('<div id="va-variant"></div>');

        this.width = ($(this.div).width());

        if (this.border) {
            var border = (Utils.isString(this.border)) ? this.border : '1px solid lightgray';
            $(this.div).css({border: border});
        }


        this.eastPanelId = this.id + "_eastPanelID";
        this.centerPanelId = this.id + "_centerPanelID";

        //SAVE CONFIG IN COOKIE
        $(window).unload(function () {
            var value = {
                species: {
                    name: _this.genomeViewer.speciesName,
                    species: _this.genomeViewer.species,
                    chromosome: _this.genomeViewer.chromosome,
                    position: _this.genomeViewer.position}
            };
            $.cookie("gm_settings", JSON.stringify(value), {expires: 365});
        });

        this.rendered = true;
    },
    draw: function () {
        if (!this.rendered) {
            console.info('Variant is not rendered yet');
            return;
        }
        var _this = this;

//      /* Header Widget */
        this.headerWidget = this._createHeaderWidget('va-header-widget');
        this.mainPanel = this._createMainPanel('va-variant');
        this.jobListWidget = this._createJobListWidget(this.eastPanelId);


        //check login
        if ($.cookie('bioinfo_sid') != null) {
            this.sessionInitiated();
        } else {
            this.sessionFinished();
        }
    },
    _createHeaderWidget: function (targetId) {
        var _this = this;
        var headerWidget = new HeaderWidget({
            targetId: targetId,
            autoRender: true,
            appname: this.title,
            description: this.description,
//            version: this.version,
            suiteId: this.suiteId,
            accountData: this.accountData
        });
        /**Atach events i listen**/
        headerWidget.onLogin.addEventListener(function (sender) {
            Ext.example.msg('Welcome', 'You logged in');
            _this.sessionInitiated();
        });

        headerWidget.onLogout.addEventListener(function (sender) {
            Ext.example.msg('Good bye', 'You logged out');
            _this.sessionFinished();
        });

        headerWidget.onGetAccountInfo.addEventListener(function (evt, response) {
            _this.setAccountData(response);
        });
        headerWidget.draw();

        return headerWidget;
    },
    _createJobListWidget: function (targetId) {
        var _this = this;
        var jobListWidget = new JobListWidget({
            "timeout":4000,
            "suiteId":this.suiteId,
            "tools":this.tools,
            "pagedViewList":{
                "title": 'Jobs',
                "pageSize": 7,
                "targetId": targetId,
                "order" : 0,
                "width": 280,
                "height": 650,
                "mode":"view"
            }
        });
        //this.dataListWidget = new DataListWidget({
        //"timeout":4000,
        //"suiteId":this.suiteId,
        //"pagedViewList":{
        //"title": 'Data',
        //"pageSize": 7,
        //"targetId": this.eastPanelId,
        //"order" : 1,
        //"width": 280,
        //"height": 650,
        //"mode":"view"  //allowed grid | view
        //}
        //});


        /**Atach events i listen**/
        jobListWidget.pagedListViewWidget.onItemClick.addEventListener(function (sender, record){
            _this.jobItemClick(record);
        });

        //this.dataListWidget.pagedListViewWidget.onItemClick.addEventListener(function (sender, record){
        //_this.dataItemClick(record);
        //});
        /***************************************/
        /***************************************/
        /***************************************/
        return jobListWidget;
    },
    _createMainPanel: function (targetId) {
            var suiteInfo =  '<div style=" width: 800px;">'
                +'<h2>Overview</h2><span align="justify">VARIANT (VARIant ANalysis Tool) can report the functional properties of any variant in all the human, mouse or rat genes (and soon new model organisms will be added) and the corresponding neighborhoods. Also other non-coding extra-genic regions, such as miRNAs are included in the analysis.<br><br>	VARIANT not only reports the obvious functional effects in the coding regions but also analyzes noncoding SNVs situated both within the gene and in the neighborhood that could affect different regulatory motifs, splicing signals, and other structural elements. These include: Jaspar regulatory motifs, miRNA targets, splice sites, exonic splicing silencers, calculations of selective pressures on the particular polymorphic positions, etc.</span>'
                +'<br><br><br>'
                +'<p align="justify"><h2>Note</h2>This web application makes an intensive use of new web technologies and standards like HTML5, so browsers that are fully supported for this site are: Chrome 14+, Firefox 7+, Safari 5+ and Opera 11+. Older browser like Chrome13-, Firefox 5- or Internet Explorer 9 may rise some errors. Internet Explorer 6 and 7 are no supported at all.</p>'
                +'</div>';

            var loginInfo='<br><br><h2>Sign in</h2><p style=" width: 800px;">You must be logged in to use this Web application, you can <b><i>register</i></b> or use a <b><i>anonymous user</i></b> as shown in the following image by clicking on the <b><i>"Sign in"</i></b> button on the top bar</p><br><div style="float:left;"><img src="http://jsapi.bioinfo.cipf.es/libs/resources/img/loginhelpbutton.png"></div><img src="http://jsapi.bioinfo.cipf.es/libs/resources/img/loginhelp.png">';
            var homeLeft = Ext.create('Ext.panel.Panel', {
                //		title:'Home',
                padding : 30,
                border:false,
                autoScroll:true,
                html: suiteInfo+loginInfo,
                bodyPadding:30,
                flex:1
            });
            var homepanel = Ext.create('Ext.panel.Panel', {
                //		padding : 30,
//			margin:"10 0 0 0",
                title:'Home',
                //		html: suiteInfo,
                border:0,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [homeLeft]
            });
            var centerPanel = Ext.create('Ext.tab.Panel', {
                id: this.centerPanelId,
                region: 'center',
                border:false,
                plain: true,
                activeTab: 0,
                items : homepanel
            });

            var mainPanel = Ext.create('Ext.panel.Panel', {
                layout: 'border',
                border:false,
                bodyStyle: 'background:ghostwhite;',
//                flex:1,
                height:this.height-this.headerWidget.height-15,
                renderTo:targetId,
                items:[centerPanel,this.getMenu(),
                    {
                        xtype:'panel',
                        id: this.eastPanelId,
                        region: 'east',
                        style : 'border: 0',
                        //title: 'Jobs & Data list',
                        title: 'Jobs',
                        collapsible : true,
                        titleCollapse: true,
                        animCollapse : false,
                        width:280,
                        collapseDirection:'top',
                        activeTab:0
                    }]
            });
        return mainPanel;
    }
}

Variant.prototype.sessionInitiated = function(){
	/*action buttons*/
	Ext.getCmp(this.id+"btnVariantEffect").enable();
	Ext.getCmp(this.id+"btnVcfViewer").enable();
	Ext.getCmp(this.id+"btnVCFTools").enable();
	
	Ext.getCmp(this.eastPanelId).expand();//se expande primero ya que si se hide() estando collapsed peta.
	Ext.getCmp(this.eastPanelId).show();

//	this.jobListWidget.draw();
	//this.dataListWidget.draw();
	
};

Variant.prototype.sessionFinished = function(){
	/*action buttons*/
	Ext.getCmp(this.id+"btnVariantEffect").disable();
	Ext.getCmp(this.id+"btnVcfViewer").disable();
    Ext.getCmp(this.id+"btnVCFTools").disable();
	
	Ext.getCmp(this.eastPanelId).expand(); //se expande primero ya que si se hide() estando collapsed peta.
	Ext.getCmp(this.eastPanelId).hide();
	this.jobListWidget.clean();
	//this.dataListWidget.clean();
	
	while(Ext.getCmp(this.centerPanelId).items.items.length>1){
		Ext.getCmp(this.centerPanelId).remove(Ext.getCmp(this.centerPanelId).items.items[Ext.getCmp(this.centerPanelId).items.items.length-1]);
	}

//	console.log(this.centerPanel.items.items)
//	this.centerPanel.removeChildEls(function(o) { return o.title != 'Home'; });
};

Variant.prototype.setAccountData = function(response) {
    this.accountData = response;
    this.jobListWidget.setAccountData(this.accountData);
};

Variant.prototype.setSize = function(width,height){
	if(width<500){width=500;}
	if(width>2400){width=2400;}//if bigger does not work TODO why?
	
	this.width=width;
	this.height=height;
	
	this._wrapPanel.setSize(width,height);
	
	this.getPanel().setSize(width,height-this.headerWidget.height);
	if(this.genomeViewer!=null){
		this.genomeViewer.setSize(Ext.getCmp(this.id+"_vcfViewer").getWidth(),Ext.getCmp(this.id+"_vcfViewer").getHeight());
	}
	
	this.headerWidget.setWidth(width);
	
	if(Ext.getCmp(this.jobListWidget.pagedListViewWidget.id+"view")!=null){
		Ext.getCmp(this.jobListWidget.pagedListViewWidget.id+"view").setSize(null,height-200);
		Ext.getCmp(this.dataListWidget.pagedListViewWidget.id+"view").setSize(null,height-200);
	}
};

///** appInterface **/
//Variant.prototype.draw = function(){
//
//	if(this._wrapPanel==null){
//		this._wrapPanel = Ext.create('Ext.panel.Panel', {
//			renderTo:this.targetId,
////			renderTo:Ext.getBody(),
////			layout: {type:'vbox', align:'strech'},
//			border:0,
//			width:this.width,
//			height:this.height,
//			items: [this.headerWidget.getPanel(),this.getPanel()]
//		});
//	}
//	if($.cookie('bioinfo_sid') != null){
//		this.sessionInitiated();
//	}else{
//		this.sessionFinished();
//	}
//};



/*****/
Variant.prototype.getMenu = function(){
	var _this=this;
   var menuBarItems = [
		{
			id:this.id+"btnVCFTools",
			text: 'VCF Tools',
			disabled:true,
			handler: function(){
				_this.showVCFtools();
			}
		},
		{
			id:this.id+"btnGWAS",
			text: 'GWAS',
			disabled:true,
			hidden:true,
			handler: function(){
				
			}
		},
		{
			id:this.id+"btnVariantEffect",
			text: 'Variant effect',
			disabled:true,
			handler: function(){
				_this.showVariantEffect();
			}
		},
		{
			id:this.id+"btnVcfViewer",
		    text: 'VCF Viewer',
			disabled:true,
			handler: function(){
				_this.showVCFviewer();
		    }
		}
    ];
	var menubar = new Ext.create('Ext.toolbar.Toolbar', {
		dock: 'top',
        cls: 'gm-navigation-bar',
        border:false,
		height:27,
		region:'north',
		minHeight:27,
		maxHeight:27,
		items:menuBarItems
	});
	return menubar;
};


Variant.prototype.jobItemClick = function (record){
	this.jobId = record.data.id;
	var _this=this;
	if(record.data.visites >= 0 ){
		
		if(!Ext.getCmp(this.eastPanelId).isHidden() || Ext.getCmp(this.eastPanelId).collapsed){
			Ext.getCmp(this.eastPanelId).collapse();
		}
		
		resultWidget = new ResultWidget({targetId:this.centerPanelId,application:'variant',app:this});
//		resultWidget.onRendered.addEventListener(function (sender, targetId){
//			_this.createGenomeMaps(targetId);
//		});
		resultWidget.draw($.cookie('bioinfo_sid'),record);
		//TODO: borrar
		this.resultWiget = resultWidget;
		
//		this.resultWidget.draw($.cookie('bioinfo_sid'),record);
	}
};
Variant.prototype.dataItemClick = function (record){
//	console.log(record.data.name);
//	_this.adapter.-------(record.data.DATAID, "js", $.cookie('bioinfo_sid'));	
};


Variant.prototype.showVariantEffect= function (){
	var _this=this;
	var variantEffectForm = new VariantEffectForm(_this);
	if(Ext.getCmp(variantEffectForm.panelId)==null){
		var pan = variantEffectForm.draw({title:"Variant effect"});
		Ext.getCmp(this.centerPanelId).add(pan);
	}
	Ext.getCmp(this.centerPanelId).setActiveTab(Ext.getCmp(variantEffectForm.panelId));
};

Variant.prototype.showVCFtools= function (){
	var _this=this;
	vcfToolsJobFormPanel = new VCFToolsJobFormPanel({suiteId:this.suiteId});
	if(Ext.getCmp(vcfToolsJobFormPanel.panelId)==null){
		vcfToolsJobFormPanel.draw();
		Ext.getCmp(this.centerPanelId).add(vcfToolsJobFormPanel.panel);
		vcfToolsJobFormPanel.onRun.addEventListener(function(sender,data){
			Ext.getCmp(_this.eastPanelId).expand();
		});
	}
	Ext.getCmp(this.centerPanelId).setActiveTab(Ext.getCmp(vcfToolsJobFormPanel.panelId));
};

Variant.prototype.showVCFviewer = function (){
	var _this=this;
	this.vcfViewer = Ext.getCmp(this.id+"_vcfViewer");
	if(this.vcfViewer==null){
		//Collapse to calculate width for genomeMaps
		pan = 26;
		if(!Ext.getCmp(this.eastPanelId).isHidden() || Ext.getCmp(this.eastPanelId).collapsed){
			Ext.getCmp(this.eastPanelId).collapse();
			pan=0;
		}
		var genomeMapsContainer = Ext.create('Ext.container.Container', {
			id:this.id+'contVCFViewer'
		});
		
		this.vcfViewer = Ext.create('Ext.panel.Panel', {
			id:this.id+"_vcfViewer",
			border: false,
		    title: "VCF Viewer",
		    closable:true,
		    items: genomeMapsContainer
//		    autoScroll:true
		});
		
		Ext.getCmp(this.centerPanelId).add(this.vcfViewer);

		//Once actived, the div element is visible, and genomeMaps can be rendered
		Ext.getCmp(this.centerPanelId).setActiveTab(this.vcfViewer);
//				
//		console.log(this.vcfViewer.getWidth());
//		console.log(this.vcfViewer.getHeight());
		

		//Parse query params to get location.... Also in AVAILABLE_SPECIES, an example location is set.
		var url = $.url();
		var location = url.param('location');
		if(location != null) {
			var position = location.split(":")[1];
			var chromosome = location.split(":")[0];
		}

		this.genomeViewer = new GenomeViewer(this.id+"contVCFViewer", DEFAULT_SPECIES,{
            availableSpecies: AVAILABLE_SPECIES,
            sidePanelCollapsed:true,
			width:this.vcfViewer.getWidth(),
			height:this.vcfViewer.getHeight()
		});
		
		//var toolbarMenu = Ext.create('Ext.toolbar.Toolbar', {
			//cls:'bio-menubar',
			//height:27,
			//padding:'0 0 0 10',
			//margins : '0 0 0 5',
			//items : [
		 		//{
					//text : 'Add track from VCF file',
					//handler : function() {
						//var vcfFileWidget = new VCFFileWidget({viewer:_this.genomeViewer});
						//vcfFileWidget.draw();
						//vcfFileWidget.onOk.addEventListener(function(sender, event) {
							//console.log(event.fileName);
							//var vcfTrack = new TrackData(event.fileName,{
								//adapter: event.adapter
							//});
							//_this.genomeViewer.addTrack(vcfTrack,{
								//id:event.fileName,
								//featuresRender:"MultiFeatureRender",
									//histogramZoom:80,
								//height:150,
								//visibleRange:{start:0,end:100},
								//featureTypes:FEATURE_TYPES
							//});
						//});
					//}
				//} 
			//]
		//});
		//this.genomeViewer.setMenuBar(toolbarMenu);
		//this.genomeViewer.afterRender.addEventListener(function(sender,event){
			//_this.setTracks(_this.genomeViewer);
		//});
		this.genomeViewer.afterRender.addEventListener(function(sender,event){
			_this.setTracks(_this.genomeViewer);
			_this.genomeViewer.addSidePanelItems(_this.getGMSidePanelItems());
		});
		this.genomeViewer.draw();
	}else{
		Ext.getCmp(this.centerPanelId).setActiveTab(this.vcfViewer);
	}
};

Variant.prototype.setTracks = function(genomeViewer){
	var geneTrack = new TrackData("gene",{
		adapter: new CellBaseAdapter({
			category: "genomic",
			subCategory: "region",
			resource: "gene",
			species: genomeViewer.species,
			featureCache:{
				gzip: true,
				chunkSize:50000
			}
		})
	});
	genomeViewer.trackSvgLayoutOverview.addTrack(geneTrack,{
		id:"gene",
		type:"gene",
		featuresRender:"MultiFeatureRender",
		histogramZoom:10,
		labelZoom:20,
		height:150,
		visibleRange:{start:0,end:100},
		titleVisibility:'hidden',
		featureTypes:FEATURE_TYPES
	});
	//end region track
	
	
	var seqtrack = new TrackData("Sequence",{
		adapter: new SequenceAdapter({
			category: "genomic",
			subCategory: "region",
			resource: "sequence",
			species: genomeViewer.species,
			featureCache:{
				gzip: true,
				chunkSize:1000
			}
		})
	});
	genomeViewer.addTrack(seqtrack,{
		id:"1",
		type:"Sequence",
		title:"Sequenece",
		featuresRender:"SequenceRender",
		height:30,
		visibleRange:{start:100,end:100}
	});

	var geneTrack = new TrackData("Gene/Transcript",{
		adapter: new CellBaseAdapter({
			category: "genomic",
			subCategory: "region",
			resource: "gene",
			species: genomeViewer.species,
			featureCache:{
				gzip: true,
				chunkSize:50000
			}
		})
	});
	genomeViewer.addTrack(geneTrack,{
		id:"2",
		type:"Gene/Transcript",
		title:"Gene/Transcript",
		featuresRender:"GeneTranscriptRender",
		histogramZoom:20,
		transcriptZoom:50,
		height:24,
		visibleRange:{start:0,end:100},
		featureTypes:FEATURE_TYPES
	});
};

Variant.prototype.addFileTrack = function(text) {
	var  _this = this;
	var fileWidget = null;
	switch(text){
		case "VCF":  fileWidget = new VCFFileWidget({viewer:_this.genomeViewer}); break;
	}
	if(fileWidget != null){
		fileWidget.draw();
		if (_this.wum){
			_this.headerWidget.onLogin.addEventListener(function (sender){
				fileWidget.sessionInitiated();
			});
			_this.headerWidget.onLogout.addEventListener(function (sender){
				fileWidget.sessionFinished();
			});
		}
		fileWidget.onOk.addEventListener(function(sender, event) {
			var fileTrack = new TrackData(event.fileName,{
				adapter: event.adapter
			});

			var id = Math.round(Math.random()*10000);
			var type = text;
			
			_this.genomeViewer.addTrack(fileTrack,{
				id:id,
				title:event.fileName,
				type:type,
				featuresRender:"MultiFeatureRender",
	//					histogramZoom:80,
				height:150,
				visibleRange:{start:0,end:100},
				featureTypes:FEATURE_TYPES
			});
			
			var title = event.fileName+'-'+id;
			//updateActiveTracksPanel(type, title, id, true);
		});
	}
};

Variant.prototype.getGMSidePanelItems = function() {
	var _this = this;
	var st = Ext.create('Ext.data.TreeStore',{
	root:{
		expanded: true,
		children: [
			{ text: "VCF", iconCls:"icon-blue-box", leaf:true}
		]
	}
	});
	return [{
			xtype:"treepanel",
			id:this.id+"availableTracksTree",
			title:"Add VCF track",
			bodyPadding:"10 0 0 0",
			useArrows:true,
			rootVisible: false,
			hideHeaders:true,
			store: st,
			listeners : {
				itemclick : function (este, record, item, index, e, eOpts){
					if(record.isLeaf()){
						_this.addFileTrack("VCF");
					}
				}
			}
	},

	];
}
