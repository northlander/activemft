'use strict';

angular.module('activemftApp')
    .controller('TransferEventDetailController', function ($scope, $stateParams, TransferEvent, TransferJob) {
        $scope.transferEvent = {};
        $scope.load = function (id) {
            TransferEvent.get({id: id}, function(result) {
              $scope.transferEvent = result;
            });
        };
        $scope.load($stateParams.id);
    });
