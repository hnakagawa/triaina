(function(){
var BasicPage = (function(){
    var Klass = function(){
        this.initialize();
    };
    (function(){
        this.initialize = function(){
	    this._toastShortBtn = $('#toast_short_btn').click(this._showToastShort);
	    this._toastShortBtn = $('#toast_long_btn').click(this._showToastLong);
	    this._toastCancelBtn = $('#toast_cancel_btn').click(this._cancelToast);

	    this._vibraterBtn = $('#vibrator_btn').click(this._vibrate);
	    this._vibraterCancelBtn = $('#vibrator_cancel_btn').click(this._cancelVibrater);

	    this._wifiEnableBtn = $('#wifi_enable_btn').click(this._enableWiFi);
	    this._wifiDisableBtn = $('#wifi_disable_btn').click(this._disableWiFi);
	    this._wifiGetMacBtnBtn = $('#wifi_mac_btn').click(this._getMacAddress);

	    this._wifiGetMacBtnBtn = $('#notification_btn').click(this._notify);
        };
        this._showToastShort = function(){
	    WebBridge.notify('system.toast.show', {'text': 'Hello!!', 'duration': 0});
        };
	this._showToastLong = function(){
	    WebBridge.notify('system.toast.show', {'text': 'Hellooooooo!!', 'duration': 1});
        };
	this._cancelToast = function(){
	    WebBridge.notify('system.toast.cancel');
        };

	this._vibrate = function() {
	    WebBridge.notify('system.vibrator.vibrate', {'msec': 5000});
	};
	this._cancelVibrater = function() {
	    WebBridge.notify('system.vibrator.cancel');
	};

	this._enableWiFi = function() {
	    WebBridge.notify('system.wifi.enable');
	};
	this._disableWiFi = function() {
	    WebBridge.notify('system.wifi.disable');
	};
	this._getMacAddress = function() {
	    WebBridge.call('system.wifi.mac.get', function(res) {
		console.log(res);
	    });
	};
	this._notify = function() {
	    WebBridge.call('system.notification.notify', {'priority': 1, 'title': 'Hello', 'text': 'world'}, function(res) {
		console.log(res);
	    });
	};
    }).apply(Klass.prototype);
    return Klass;
})();

var page = new BasicPage();

})();

