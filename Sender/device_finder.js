
function sendBroadcast(socketId) {
    magic = str2ab("caster");
    chrome.sockets.udp.send(socketId, magic, "225.0.0.0", 2278, function (result) { });
}

function enableBroadCast(socketId) {
    chrome.sockets.udp.setBroadcast(socketId, true, function (result) {
        sendBroadcast(socketId);
    });
}

function bindSocket(socketId) {
    chrome.sockets.udp.bind(socketId, "0.0.0.0", 0, function (result) {
        enableBroadCast(socketId);
    });
}

function discoverDevice() {
    chrome.sockets.udp.create({}, function (createInfo) {
        bindSocket(createInfo.socketId);
    });
}

chrome.sockets.udp.onReceive.addListener(
    function (info) {
        name = ab2str(info.data);
        address = info.remoteAddress;
        port = info.remotePort;

        onDeviceFound(name, address, port);
    }
);
