function str2ab(str) {
    var buf = new ArrayBuffer(str.length);
    var bufview = new Uint8Array(buf);
    for (var i = 0; i < str.length; i++) {
        bufview[i] = str.charCodeAt(i);
    }
    return buf;
}

function ab2str(buf) {
    if (buf == undefined) {
        return "Unknown";
    }

    return String.fromCharCode.apply(null, new Uint8Array(buf));
}
