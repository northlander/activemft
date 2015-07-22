'use strict';

angular.module('activemftApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('transferEvent', {
                parent: 'entity',
                url: '/transferEvent',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'activemftApp.transferEvent.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/transferEvent/transferEvents.html',
                        controller: 'TransferEventController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('transferEvent');
                        return $translate.refresh();
                    }]
                }
            })
            .state('transferEventDetail', {
                parent: 'entity',
                url: '/transferEvent/:id',
                data: {
                    roles: ['ROLE_USER'],
                    pageTitle: 'activemftApp.transferEvent.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/transferEvent/transferEvent-detail.html',
                        controller: 'TransferEventDetailController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('transferEvent');
                        return $translate.refresh();
                    }]
                }
            });
    });
