function Bierapp(args) {
    _.extend(this, Backbone.Events);

    var _this = this;
    this.id = Utils.genId("Bierapp");

    //set default args
    this.suiteId = 22;
    this.title = 'BIERapp';
    this.description = '';
    this.version = '1.0.1';
    this.tools = ["pathiways"];
    this.border = true;
    this.targetId;
    this.width;
    this.height;


    //set instantiation args, must be last
    _.extend(this, args);

    this.accountData = null;

    this.resizing = false;

    this.rendered = false;
    if (this.autoRender) {
        this.render();
    }
}

Bierapp.prototype = {
    render: function (targetId) {
        var _this = this;
        this.targetId = (targetId) ? targetId : this.targetId;
        if ($('#' + this.targetId).length < 1) {
            console.log('targetId not found in DOM');
            return;
        }

        console.log("Initializing Bierapp");
        this.targetDiv = $('#' + this.targetId)[0];
        this.div = $('<div id="pathiways" style="height:100%;"></div>')[0];
        $(this.targetDiv).append(this.div);

        $(this.div).append('<div id="header-widget"></div>');
        $(this.div).append('<div id="menu"></div>');
        this.wrapDiv = $('<div id="wrap" style="height:100%;position:relative;"></div>')[0];
        $(this.div).append(this.wrapDiv);


        this.sidePanelDiv = $('<div id="sidePanel" style="position:absolute; z-index:50;right:0px;"></div>')[0];
        $(this.wrapDiv).append(this.sidePanelDiv);

        this.panelDiv = $('<div id="panel"></div>')[0];
        $(this.panelDiv).css({
            width: '100%'
        });
        $(this.wrapDiv).append(this.panelDiv);


        this.width = ($(this.div).width());
        this.height = ($(this.div).height());

        if (this.border) {
            var border = (_.isString(this.border)) ? this.border : '1px solid lightgray';
            $(this.div).css({border: border});
        }

        $(window).resize(function (event) {
            if (event.target == window) {
                if (!_this.resizing) {//avoid multiple resize events
                    _this.resizing = true;
                    _this.setSize($(_this.div).width(), $(_this.div).height());
                    setTimeout(function () {
                        _this.resizing = false;
                    }, 400);
                }
            }
        });

        this.rendered = true;
    },
    draw: function () {
        var _this = this;
        if (!this.rendered) {
            console.info('Bierapp is not rendered yet');
            return;
        }

        /* Header Widget */
        this.headerWidget = this._createHeaderWidget('header-widget');

        /* Menu */
        this.menu = this._createMenu('menu');


        var topOffset = $('#header-widget').height() + $('#menu').height();
        $(this.panelDiv).css({height: 'calc(100% - ' + topOffset + 'px)'});


        /* Job List Widget */
        this.jobListWidget = this._createJobListWidget('sidePanel');

        /* Bier Panel */
        this.variantWidget = this._createVariantWidget('panel');


        /*check login*/
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
            version: this.version,
            suiteId: this.suiteId,
            accountData: this.accountData,
            allowLogin:false
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
    _createMenu: function (targetId) {
        var _this = this;
        var toolbar = Ext.create('Ext.toolbar.Toolbar', {
            id: this.id + "navToolbar",
            renderTo: targetId,
            cls: 'gm-navigation-bar',
            region: "north",
            width: '100%',
            border: false,
            items: [
//                {
//                    id: this.id + "btnPathi",
////                    disabled: true,
//                    text: '<span class="link emph">Press to run PATHiWAYS<span>',
//                    handler: function () {
//                        _this.showPathi();
//                    }
//                },
                '->'
                ,
                {
                    id: this.id + 'jobsButton',
                    disabled:true,
                    tooltip: 'Show Jobs',
                    text: '<span class="emph"> Hide jobs </span>',
                    enableToggle: true,
                    pressed: true,
                    toggleHandler: function () {
                        if (this.pressed) {
                            this.setText('<span class="emph"> Hide jobs </span>');
                            _this.jobListWidget.show();
                        } else {
                            this.setText('<span class="emph"> Show jobs </span>');
                            _this.jobListWidget.hide();
                        }
                    }
                }
            ]
        });
        return toolbar;
    },
    _createVariantWidget: function (targetId) {
        var _this = this;

        var variantWidget = new VariantWidget({
            targetId: targetId,
            autoRender:true
        });
        variantWidget.draw();

        return variantWidget;
    },
    _createJobListWidget: function (targetId) {
        var _this = this;

        var jobListWidget = new JobListWidget({
            'timeout': 4000,
            'suiteId': this.suiteId,
            'tools': this.tools,
            'pagedViewList': {
                'title': 'Jobs',
                'pageSize': 7,
                'targetId': targetId,
                'order': 0,
                'width': 280,
                'height': 625,
                border: true,
                'mode': 'view'
            }
        });

        /**Atach events i listen**/
        jobListWidget.pagedListViewWidget.onItemClick.addEventListener(function (sender, record) {
            _this.jobItemClick(record);
        });
        jobListWidget.draw();

        return jobListWidget;
    },
    sessionInitiated: function () {
        Ext.getCmp(this.id + 'jobsButton').enable();
        Ext.getCmp(this.id + 'jobsButton').toggle(true);
        //this.jobListWidget.draw();
        //this.dataListWidget.draw();
    },
    sessionFinished : function () {
    Ext.getCmp(this.id + 'jobsButton').disable();
    Ext.getCmp(this.id + 'jobsButton').toggle(false);

    this.jobListWidget.clean();
    this.accountData = null;

//    this.panel.items.each(function (child) {
//        if (child.title != 'Home') {
//            child.destroy();
//        }
//    })
}
}



Bierapp.prototype.setAccountData = function (response) {
    this.accountData = response;
    this.jobListWidget.setAccountData(this.accountData);
};

Bierapp.prototype.setSize = function (width, height) {
    this.width = width;
    this.height = height;

    this.headerWidget.setWidth(width);
    this.menu.setWidth($(this.menuDiv).width());
    this.panel.setWidth($(this.panelDiv).width());
};

Bierapp.prototype.jobItemClick = function (record) {
    var _this = this;
    this.jobId = record.data.id;
    if (record.data.visites >= 0) {

        Ext.getCmp(this.id + 'jobsButton').toggle(false);

        var resultWidget = new ResultWidget({targetId: this.panel.getId(), application: 'pathiway', app: this});
        resultWidget.draw($.cookie('bioinfo_sid'), record);
    }
};


Bierapp.prototype.showPathi = function () {
    var _this = this;
    var showForm = function () {
        var pathiwaysForm = new BierappForm(_this);
        if (Ext.getCmp(pathiwaysForm.panelId) == null) {
            var panel = pathiwaysForm.draw({title: "PATHiWAYS"});
            _this.panel.add(panel);
        }
        _this.panel.setActiveTab(Ext.getCmp(pathiwaysForm.panelId));
    };

    if (!$.cookie('bioinfo_sid')) {
        _this.headerWidget.onLogin.addEventListener(function (sender, data) {
            showForm();
        });
        _this.headerWidget.loginWidget.anonymousSign();
    } else {
        showForm();
    }

};

