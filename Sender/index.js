var SENDER_EXT_ID = "blpfloafflongalcjfmckamakiddjapo";


function enableSendButton() {
    $("#sendButton").prop("disabled", false);
}

function addDevice(deviceName, ip, port) {
    var content = deviceName + " (" + ip + ":" + port + ")";
    var timestamp = new Date().getTime();
	var attrs = {
		type : "radio",
		name : "selectedDevice",
		value: deviceName,
		ip : ip,
		port: port,
		id: timestamp
	};
	var device = $("<input />").attr(attrs);
    $(device).bind('click', enableSendButton);

	var label = $("<label />").attr("for", timestamp).html(content);
	var br = $("<br />");

	$("#devices").append(device);
	$("#devices").append(label);
	$("#devices").append(br);

	$("#devices").buttonset();
}

function clearDevice() {
    $("#devices").html("");
}

function closeWindow() {
    var appWindow = chrome.app.window.get("CasterSender");
    if (appWindow != null) {
        appWindow.close();
    }
}

function send(url, ip, port) {
    if (url.search("bilibili.com") >= 0) {
        getBilibiliUrl(url, function (result) {
            $.get("http://" + ip + ":" + port + "/playmp4?url=" + result, closeWindow);
        });
    }
    else if (url.search("www.youtube.com/watch") >= 0) {
        $.get("http://" + ip + ":" + port + "/youtube?url=" + encodeURIComponent(url), closeWindow);
    }
}

function onSendClick(event) {
	var radio = $("#devices > input:checked");
    if (radio.size() == 0) {
        return;
    }

	var url = $("#urlText").val();

	send(url, radio.attr("ip"), radio.attr("port"));
}

// Called by device_finder.js
function onDeviceFound(name, address, port) {
    addDevice(name, address, port);
}

// receive URL from extension
chrome.runtime.onMessageExternal.addListener(
    function (message, sender, response) {
        $("#urlText").attr("value", message);
    }
);

$(document).ready(function () {
    $("#sendButton").bind("click", onSendClick);

    chrome.runtime.sendMessage(SENDER_EXT_ID, "");  // notify extension we are ready

    // find cast receiver
    clearDevice();
    discoverDevice();
});


