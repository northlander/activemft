'use strict';

angular.module('activemftApp')
    .factory('TransferJob', function ($resource, DateUtils) {
        return $resource('api/transferJobs/:id', {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    });
