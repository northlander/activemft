'use strict';

angular.module('activemftApp')
    .controller('TransferEventController', function ($scope, TransferEvent, TransferJob, TransferEventSearch, ParseLinks) {
        $scope.transferEvents = [];
        $scope.transferjobs = TransferJob.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            TransferEvent.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.transferEvents = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            TransferEvent.get({id: id}, function(result) {
                $scope.transferEvent = result;
                $('#saveTransferEventModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.transferEvent.id != null) {
                TransferEvent.update($scope.transferEvent,
                    function () {
                        $scope.refresh();
                    });
            } else {
                TransferEvent.save($scope.transferEvent,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            TransferEvent.get({id: id}, function(result) {
                $scope.transferEvent = result;
                $('#deleteTransferEventConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            TransferEvent.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteTransferEventConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.search = function () {
            TransferEventSearch.query({query: $scope.searchQuery}, function(result) {
                $scope.transferEvents = result;
            }, function(response) {
                if(response.status === 404) {
                    $scope.loadAll();
                }
            });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveTransferEventModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.transferEvent = {transferId: null, state: null, timestamp: null, size: null, filename: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
