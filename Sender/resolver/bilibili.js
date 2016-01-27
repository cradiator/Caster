var BILIBILI_APPKEY = "85eb6835b0a1034e";
var BILIBILI_APPSEC = "2ad42749773c441109bdc0191257a664";


function getBilibiliUrl(url, callback) {
	
	var pattern = new RegExp("http:/*[^/]+/video/av(\\d+)(/|/index.html|/index_(\\d+).html)?(\\?|#|$)", "i");
	var match = pattern.exec(url);
	if (match == null || match == undefined) {
		console.log(url + " not a valid bilibili url");
		return;
	}

	var aid = match[1];
	var pid = match[3];
	if (pid == undefined) {
		pid = "1";
	}
	var cid_args = {'type': 'json', 'id': aid, 'page': pid};
	var sign = getBilibiliSign(cid_args, BILIBILI_APPKEY, BILIBILI_APPSEC);
	var resp_cid = "http://api.bilibili.com/view?" + sign;
	$.get(resp_cid, function(data, textStatus, xhr) {
		if (textStatus != "success") {
			console.log("get bilibili resp_cid fail");
			return;
		}

		parseBilibiliCid(data, callback);
	});
}

function parseBilibiliCid(respCid, callback) {
	var cid = respCid.cid;
	var media_args = {'otype': 'json', 'cid': cid, 'type': 'mp4', 'quality': 4, 'appkey': BILIBILI_APPKEY};
	var url_get_media = "http://interface.bilibili.com/playurl?";
	var data = "";
	for(var key in media_args) {
		if (data.length > 0) {
			data += "&";
		}
		data += key;
		data += "=";
		data += media_args[key];
	}
	url_get_media += data;

	$.get(url_get_media, function(data, textStatus, xhr) {
		if (textStatus != "success") {
			console.log("get bilibili media fail");
			return;
		}

		parseBilibiliMediaUrl(data, callback);
	})
}

function parseBilibiliMediaUrl(result, callback) {
	if (result.result == "error") {
		console.log("bilibili return error when retrieve media url");
		return;
	}

	media_url = result.durl[0].url;
	console.log("get bilibili url " + media_url);

	if (callback) {
		callback(media_url);
	}
}

function getBilibiliSign(args, appkey, appsecret) {
	args["appkey"] = appkey;
	var keys = Object.keys(args).sort();
	var data = "";
	for(var i = 0; i < keys.length; i++) {
		if (data.length != 0) {
			data += "&";
		}
		var key = keys[i];
		data += key;
		data += "=";
		data += args[key];
	}

	if (appsecret == undefined || appsecret.length == 0) {
		return data;
	}

	var hash = md5(data + appsecret);
	return data + "&sign=" + hash;
}
