'use strict';

angular.module('activemftApp')
    .factory('TransferJobSearch', function ($resource) {
        return $resource('api/_search/transferJobs/:query', {}, {
            'query': { method: 'GET', isArray: true}
        });
    });
