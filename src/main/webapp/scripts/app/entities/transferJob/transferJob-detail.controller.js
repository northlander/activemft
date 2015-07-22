'use strict';

angular.module('activemftApp')
    .controller('TransferJobDetailController', function ($scope, $stateParams, TransferJob, TransferEvent) {
        $scope.transferJob = {};
        $scope.load = function (id) {
            TransferJob.get({id: id}, function(result) {
              $scope.transferJob = result;
            });
        };
        $scope.load($stateParams.id);
    });
