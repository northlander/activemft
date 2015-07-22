'use strict';

angular.module('activemftApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
