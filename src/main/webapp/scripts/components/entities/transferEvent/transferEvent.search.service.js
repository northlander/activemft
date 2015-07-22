'use strict';

angular.module('activemftApp')
    .factory('TransferEventSearch', function ($resource) {
        return $resource('api/_search/transferEvents/:query', {}, {
            'query': { method: 'GET', isArray: true}
        });
    });
