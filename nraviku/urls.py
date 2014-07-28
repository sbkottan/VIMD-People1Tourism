#########################################################
# This file calls the correct function based on the url #
#########################################################

from django.conf.urls.defaults import patterns, include, url
from django.conf import settings

from django.views.generic.create_update import create_object

from django.contrib.auth.decorators import login_required

import peoplefirstapp.views as pfviews
from peoplefirstapp.texting import text_view
from peoplefirstapp.forms import *

urlpatterns = patterns('',

    url(r'^$', pfviews.Home.as_view(), name='home_url'),

    url(r'^mobileapi/geospatial/$',pfviews.mobile_near_me_geospatial, name='mobile_near_me_geospatial'),
    #url(r'^mobileapi/nearme/$',pfviews.mobile_near_me, name='mobile_near_me_url' ),
    url(r'^mobileapi/dashboard/$',pfviews.mobile_dashboard, name='mobile_dashboard' ),
    url(r'^mobileapi/comment/$',pfviews.mobile_comment, name='mobile_comment' ),
    url(r'^mobileapi/picture/$',pfviews.mobile_picture, name='mobile_picture' ),
    url(r'^mobileapi/login/$',pfviews.mobile_login, name='mobile_login' ),

    url(r'^mobileapi/share/$',pfviews.mobile_share, name='mobile_share'),


    url(r'^franchises/$', pfviews.FranchiseList.as_view(), name='franchise_list_url'),
    url(r'^franchises/(?P<franchise_pk>\d+)/$', pfviews.FranchiseDetails.as_view(), name='franchise_detail_url'),
    url(r'^franchises/create/$', pfviews.create_franchise, name='create_franchise_url'),

    url(r'^networks/$', pfviews.NetworkList.as_view(), name='network_list_url'),
    url(r'^networks/create/$', pfviews.create_network, name='create_network_url'),
    url(r'^networks/(?P<network_name>\w+)/$', pfviews.NetworkDetailList.as_view(), name='network_detail_list_url'),
    url(r'^networks/(?P<network_pk>\d+)/$', pfviews.NetworkDetails.as_view(), name='network_detail_url'),
    url(r'^networks/(?P<network_pk>\d+)/join/$', pfviews.join_network, name='network_join_url'),
    url(r'^networks/(?P<network_pk>\d+)/leave/$', pfviews.leave_network, name='network_leave_url'),

    url(r'^entrepreneurs/$', pfviews.EntrepreneurList.as_view(), name='entrepreneur_list_url'),
    url(r'^entrepreneurs/(?P<ent_pk>\d+)/$', pfviews.EntrepreneurDetails.as_view(), name='entrepreneur_detail_url'),
    url(r'^entrepreneurs/(?P<ent_pk>\d+)/addservice/$', pfviews.ServiceAdditionWizard.as_view([CreateServiceForm1, WizardLocationForm, WizardAddressForm]), name='add_service_wizard_url'),
    url(r'^entrepreneurs/(?P<ent_pk>\d+)/payment/$', pfviews.create_payment_gateway, name='create_gateway_url'),
    url(r'^entrepreneurs/(?P<ent_pk>\d+)/testsms/$', pfviews.test_sms, name='test_sms_url'),
    url(r'^entrepreneurs/create/$', pfviews.EntrepreneurCreationWizard.as_view([AdminCreateEntForm1, WizardLocationForm, WizardAddressForm, AdminCreateEntForm4]), name='admin_create_entrepreneur_wizard_url'),

    url(r'^agents/(?P<ea_pk>\d+)/$', pfviews.EmpowermentAgentDetails.as_view(), name='agent_detail_url'),
    url(r'^agents/create/$', pfviews.AgentCreationWizard.as_view([CreateAgentForm1, WizardLocationForm, WizardAddressForm, CreateAgentForm4, CreateAgentForm5]), name='create_agent_wizard_url'),
    url(r'^agents/addtoadmin/$', pfviews.AgentAddToAdminWizard.as_view([CreateAgentForm1, WizardLocationForm, WizardAddressForm, CreateAgentForm4]), name='add_agent_to_admin_url'),
    url(r'^agents/$', pfviews.EmpowermentAgentList.as_view(), name='agent_list_url'),

    url(r'^services/$', pfviews.ServiceList.as_view(), name='service_list_url'),
    url(r'^services/(?P<service_pk>\d+)/$', pfviews.ServiceDetails.as_view(), name='service_detail_url'),
    url(r'^services/(?P<service_pk>\d+)/offer/$', pfviews.create_offer, name='offer_create_url'),
    url(r'^services/create/$', pfviews.ServiceCreationWizard.as_view([CreateServiceForm1, WizardLocationForm, WizardAddressForm]), name='service_create_wizard_url'),

    url(r'^offers/$', pfviews.OfferList.as_view(), name='offer_list_url'),
    url(r'^offers/(?P<offer_pk>\d+)/$', pfviews.OfferDetails.as_view(), name='offer_detail_url'),
    url(r'^offers/(?P<offer_pk>\d+)/checkout/$', pfviews.checkout, name='checkout_url'),
    url(r'^offers/(?P<offer_pk>\d+)/cancel/$', pfviews.cancel_offer, name='cancel_offer_url'),
    url(r'^offers/(?P<offer_pk>\d+)/payment_complete/$', pfviews.payment_complete, name='payment_complete_url'),

    url(r'^offers/(?P<offer_pk>\d+)/canceled/$', pfviews.paypal_canceled, name='paypal_canceled_url'),
    url(r'^offers/(?P<offer_pk>\d+)/success/$', pfviews.paypal_success, name='paypal_success_url'),
    url(r'^offers/payment_validation/ipn/$', include('paypal.standard.ipn.urls')),

    url(r'^admins/create$', pfviews.create_admin, name='create_admin_url'),
    url(r'^admins/(?P<admin_pk>\d+)/$', pfviews.PeopleFirstAdminDetails.as_view(), name='admin_detail_url'),
    url(r'^admins/$', pfviews.PeopleFirstAdminList.as_view(), name='admin_list_url'),

    url(r'^accounts/login/$', 'django.contrib.auth.views.login', {'authentication_form': AuthenticateForm, 'template_name': 'peoplefirstapp/login.html'}, name='login_url'),
    url(r'^accounts/logout/$', 'django.contrib.auth.views.logout', {'next_page': '/'}, name='logout_url'),
    url(r'^accounts/activate/(?P<activation_key>[\w\d]+)/$', pfviews.user_activate_view, name='user_activate_url'),
    url(r'^accounts/manage/$', pfviews.manage_account, name='manage_account_url'),

    url(r'^accounts/create/$', pfviews.create_tourist, name='tourist_create_url'),
    url(r'^accounts/(?P<tourist_pk>\d+)/$', pfviews.TouristDetails.as_view(), name='tourist_detail_url'),

    url(r'^about/$', pfviews.About.as_view(), name='about_url'),
    url(r'^text/$', text_view),

    url(r'^travelers_info/$', pfviews.Travelers.as_view(), name='travelers_info_url'),
    url(r'^entrepreneurs_info/$', pfviews.Entrepreneurs.as_view(), name='entrepreneurs_info_url'),
    url(r'^empowerment_agents_info/$', pfviews.EmpowermentAgents.as_view(), name='empowerment_agents_info_url'),
    url(r'^safety_info/$', pfviews.Safety.as_view(), name='safety_info_url'),
    url(r'^how_p1t_works_info/$', pfviews.HowP1TWorks.as_view(), name='how_p1t_works_info_url'),

    url(r'^dashboard/$', pfviews.dashboard_view, name='dashboard_url'),

    url(r'^error413/$', pfviews.handle413),
)

if settings.DEBUG:
    from django.contrib.staticfiles.urls import staticfiles_urlpatterns
    urlpatterns += staticfiles_urlpatterns()
    urlpatterns += patterns('', url(r'^media/(?P<path>.*)$', 'django.views.static.serve', {'document_root': settings.MEDIA_ROOT,}),)
