// Copyright (c) 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// Called when the user clicks on the browser action.

var SENDER_APP_ID = "kebllpflopbijobohelfeofonkagfkhm";

chrome.browserAction.onClicked.addListener(
    function (tab) {
        chrome.management.launchApp(SENDER_APP_ID);
    }
);

chrome.runtime.onMessageExternal.addListener(
    function (message, sender) {
        chrome.tabs.query({ currentWindow: true, active: true }, function (tabs) {
            var current = tabs[0];
            chrome.runtime.sendMessage(SENDER_APP_ID, current.url);
        });
    }
);
