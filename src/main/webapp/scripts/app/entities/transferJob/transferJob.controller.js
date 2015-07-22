'use strict';

angular.module('activemftApp')
    .controller('TransferJobController', function ($scope, TransferJob, TransferEvent, TransferJobSearch, ParseLinks) {
        $scope.transferJobs = [];
        $scope.transferevents = TransferEvent.query();
        $scope.page = 1;
        $scope.loadAll = function() {
            TransferJob.query({page: $scope.page, per_page: 20}, function(result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.transferJobs = result;
            });
        };
        $scope.loadPage = function(page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.showUpdate = function (id) {
            TransferJob.get({id: id}, function(result) {
                $scope.transferJob = result;
                $('#saveTransferJobModal').modal('show');
            });
        };

        $scope.save = function () {
            if ($scope.transferJob.id != null) {
                TransferJob.update($scope.transferJob,
                    function () {
                        $scope.refresh();
                    });
            } else {
                TransferJob.save($scope.transferJob,
                    function () {
                        $scope.refresh();
                    });
            }
        };

        $scope.delete = function (id) {
            TransferJob.get({id: id}, function(result) {
                $scope.transferJob = result;
                $('#deleteTransferJobConfirmation').modal('show');
            });
        };

        $scope.confirmDelete = function (id) {
            TransferJob.delete({id: id},
                function () {
                    $scope.loadAll();
                    $('#deleteTransferJobConfirmation').modal('hide');
                    $scope.clear();
                });
        };

        $scope.search = function () {
            TransferJobSearch.query({query: $scope.searchQuery}, function(result) {
                $scope.transferJobs = result;
            }, function(response) {
                if(response.status === 404) {
                    $scope.loadAll();
                }
            });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveTransferJobModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.transferJob = {name: null, description: null, cronExpression: null, archive: null, enabled: null, sourceUrl: null, sourceFilepattern: null, sourceType: null, sourceUsername: null, sourcePassword: null, targetUrl: null, targetType: null, targetFilename: null, targetUsername: null, targetPassword: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
