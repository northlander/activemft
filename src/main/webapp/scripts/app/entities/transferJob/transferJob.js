'use strict';

angular.module('activemftApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('transferJob', {
                parent: 'entity',
                url: '/transferJob',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'activemftApp.transferJob.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/transferJob/transferJobs.html',
                        controller: 'TransferJobController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('transferJob');
                        return $translate.refresh();
                    }]
                }
            })
            .state('transferJobDetail', {
                parent: 'entity',
                url: '/transferJob/:id',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'activemftApp.transferJob.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/transferJob/transferJob-detail.html',
                        controller: 'TransferJobDetailController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('transferJob');
                        return $translate.refresh();
                    }]
                }
            });
    });
