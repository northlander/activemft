'use strict';

angular.module('activemftApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


