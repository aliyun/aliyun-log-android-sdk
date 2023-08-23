<script type="text/javascript">


var _xhrGlobalId = Math.floor(Math.random() * 100000);

/**
 * generate an unique id string (32)
 * @private
 * @return string
 */
function getUniqueID() {
//    var id = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
//        let r = Math.random() * 16 | 0,
//            v = c == 'x' ? r : (r & 0x3 | 0x8);
//        return v.toString(16);
//    });
//    return id;
    return (new Date()).getTime() + "" + _xhrGlobalId++;
}

/**
 * mock ajax request
 */
function mockAjax() {
	var _XMLHttpRequest = window.XMLHttpRequest;
	if (!_XMLHttpRequest) {
		return;
	}

	var that = this;
	var _open = window.XMLHttpRequest.prototype.open;
	var _send = window.XMLHttpRequest.prototype.send;
	var _setRequestHeader = window.XMLHttpRequest.prototype.setRequestHeader;
	var _overrideMimeType = window.XMLHttpRequest.prototype.overrideMimeType;
	var _handleResponse = function (event) {
        var headers = this.getAllResponseHeaders();
        var headersObj = {};
        headers.trim().split(/[\r\n]+/).forEach(function(line) {
            var parts = line.split(': ');
            var header = parts.shift();
            var value = parts.join(': ');
            headersObj[header] = value;
        });

        var headersJson = JSON.stringify(headersObj);
		var currentTarget = event.currentTarget;
		window.otelJsi.handleResponse(currentTarget._requestID, currentTarget.status, currentTarget.statusText, currentTarget.responseText, headersJson)
	}
	// mock open()
	window.XMLHttpRequest.prototype.open = function () {
		var XMLReq = this;
		var args = [].slice.call(arguments);
		var method = args[0];
		var url = args[1];
		var requestId = that.getUniqueID();
//		var hasQuery = url.split("?")[1];
		var urlWrap = url;
//		if (hasQuery) {
//			urlWrap = url + "&" + "otel_flag=" + requestId
//		} else {
//			urlWrap = url + "?" + "otel_flag=" + requestId
//		}
		// may be used by other functions
		XMLReq._requestID = requestId;
		XMLReq._method = method;
		XMLReq._url = urlWrap;
		args[1] = urlWrap;

		//console.log("urlWrap===>",urlWrap)
		window.otelJsi.open(requestId, urlWrap, method, window.location.origin);
		return _open.apply(XMLReq, args);
	}

	// mock send()
	window.XMLHttpRequest.prototype.send = function () {
		var XMLReq = this;
		var args = [].slice.call(arguments);
		var body = args[0];
		window.otelJsi.send(XMLReq._requestID, body);

		if (this['addEventListener']) {
			this['addEventListener']('error', _handleResponse);
			this['addEventListener']('load', _handleResponse);
			this['addEventListener']('abort', _handleResponse);
		} else {
			var _oldStateChange = this['onreadystatechange'];
			this['onreadystatechange'] = function (event) {
				if (this.readyState === 4) {
					_handleResponse(event);
				}
				_oldStateChange && _oldStateChange.apply(this, arguments);
			};
		}
		return _send.apply(XMLReq, args);
	}

	//mock  setRequestHeader()
	window.XMLHttpRequest.prototype.setRequestHeader = function () {
		var XMLReq = this;
		var args = [].slice.call(arguments);
		var header = args[0];
		var value = args[1];
		window.otelJsi.setRequestHeader(XMLReq._requestID, header, value);
		return _setRequestHeader.apply(XMLReq, args);
	}

	//mock  overrideMimeType()
	window.XMLHttpRequest.prototype.overrideMimeType = function () {
		var XMLReq = this;
		var args = [].slice.call(arguments);
		var mimeType = args[0];
		window.otelJsi.overrideMimeType(XMLReq._requestID, mimeType);
		return _overrideMimeType.apply(XMLReq, args);
	}
}


/**
 * mock fetch request
 */
function mockFetch() {
	var _fetch = window.fetch;
	if (!_fetch) {
		return;
	}
	var that = this;
	var _prevFetch = function (url, init) {
		var requestId = that.getUniqueID();
		var body = init.body;
		var method = init.method || 'GET';
		var headers = JSON.stringify(init.headers);
//		var hasQuery = url.split("?")[1];
		var urlWrap = url;
//		if (hasQuery) {
//			urlWrap = url + "&" + "otel_flag=" + requestId
//		} else {
//			urlWrap = url + "?" + "otel_flag=" + requestId
//		}
		console.log("mockFetch", requestId, urlWrap, method, window.location.origin, headers, body);
		window.otelJsi.fetch(requestId, urlWrap, method, window.location.origin, headers, body);
		return _fetch(urlWrap, init)
			.then(function (response) {
				return response.clone().text().then(function (text) {
				    var headersObjc = {}
				    response.headers.forEach(function(value, key) {
				        headersObjc[key] = value
				    })
					window.otelJsi.handleResponse(requestId, response.status, response.statusText, text, JSON.stringify(headersObjc))
					return response
				});
			})
			.catch(function (err) {
			    window.otelJsi.handleResponse(requestId, "", "", "", null)
				return err;
			});

	}
	window.fetch = _prevFetch;
}

/**
 * mock Storage & Cookie
 */
function mockStorage() {
	var _Storage = window.Storage;
	if (!_Storage) {
		return;
	}
	var that = this;
	var _setItem = window.Storage.prototype.setItem;
	var _removeItem = window.Storage.prototype.removeItem;
	var _clear = window.Storage.prototype.clear;
	// mock setItem()
	window.Storage.prototype.setItem = function () {
		var storage = this;
		var args = [].slice.call(arguments);
		if (storage === localStorage) {
			window.otelJsi.localStorageSetItem(args[0], args[1]);
		} else if (storage === sessionStorage) {
			window.otelJsi.sessionStorageSetItem(args[0], args[1]);
		}

		return _setItem.apply(storage, args);
	}

	// mock removeItem()
	window.Storage.prototype.removeItem = function () {
		var storage = this;
		var args = [].slice.call(arguments);
		if (storage === localStorage) {
			window.otelJsi.localStorageRemoveItem(args[0]);
		} else if (storage === sessionStorage) {
			window.otelJsi.sessionStorageRemoveItem(args[0]);
		}

		return _removeItem.apply(storage, args);
	}

	// mock clear()
	window.Storage.prototype.clear = function () {
		var storage = this;
		var args = [].slice.call(arguments);
		if (storage === localStorage) {
			window.otelJsi.localStorageClear();
		} else if (storage === sessionStorage) {
			window.otelJsi.sessionStorageClear();
		}

		return _clear.apply(storage, args);
	}

}


mockAjax();
mockFetch();
<!-- mockStorage(); -->

</script>