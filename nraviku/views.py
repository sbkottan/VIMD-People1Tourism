###############################################################################
## This is the file where the view for current urls is defined and 	      #
## this file passes the required JSON file to the mobile to parse and display #
###############################################################################

from django.conf import settings
from django.contrib import messages

from django.core.mail import EmailMessage
from django.core.files.storage import FileSystemStorage

from emailusernames.utils import create_user
from django.contrib.auth import authenticate, login

from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response, get_object_or_404, render
from django.template import RequestContext
from django.http import HttpResponseForbidden, HttpResponse, HttpResponseRedirect, HttpResponseNotFound, HttpResponseNotAllowed, HttpResponseBadRequest, Http404

import urllib2
from base64 import b64encode
from xml.dom.minidom import parseString

# events
from peoplefirstapp import events

# views
from django.views.generic import View, CreateView, UpdateView, FormView, DetailView, ListView, TemplateView

# models
from peoplefirstapp.models import Tourist, Entrepreneur, EmpowermentAgent, Service, Offer, Activation, PeopleFirstAdmin, Franchise, Network, Country, Mobile
from django.contrib.sites.models import Site
from django.contrib.auth.models import User

# forms
from django.contrib.auth.forms import UserCreationForm
from peoplefirstapp.forms import *
from django.contrib.formtools.wizard.views import SessionWizardView

# texting
from peoplefirstapp.texting import queue_offer_text, send_confirmation_text, initiate_test_session

# utils
from peoplefirstapp.utils import send_email, reverse_geocode

# celery
from celery.task.sets import TaskSet
from peoplefirstapp.tasks import OfferInvalidationTask

# gis
from django.contrib.gis import geos
from django.contrib.gis.measure import D
from django.contrib.gis.geos import *

# decorators
from django.contrib.auth.decorators import login_required
from django.utils.decorators import method_decorator

from django.views.decorators.csrf import csrf_exempt

# libs
import hashlib
from spreedlylib import Spreedly
import simplejson
import datetime

# paypal
from paypal.standard.forms import PayPalPaymentsForm

class Home(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/home.html'

    def get_context_data(self, **kwargs):
        context = super(Home, self).get_context_data(**kwargs)

        # get featured entrepreneurs (six chosen at random)
        context['featured_entrepreneurs'] = Entrepreneur.objects.order_by('?')[:6]
        context['entrepreneur_list'] = Entrepreneur.objects.all()

        return context


class About(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/about.html'


class Travelers(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/footer_links/travelers_info.html'


class Entrepreneurs(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/footer_links/entrepreneurs_info.html'


class EmpowermentAgents(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/footer_links/empowerment_agents_info.html'


class Safety(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/footer_links/safety_info.html'


class HowP1TWorks(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/footer_links/how_p1t_works_info.html'

##################
### MOBILE API ###
##################
@csrf_exempt
def mobile_near_me_geospatial(request):
	latitude = request.REQUEST["latitude"]
	longitude = request.REQUEST["longitude"]
	#print latitude
	#print longitude
	#longitude = -78.62
	#latitude = 35.78


	#pnt = geos.Point(float(latitude), float(longitude))
	#sm= geos.WorldBorder.objects.get(mpoly_intersects=pnt)
	pnt = fromstr("POINT(%s %s)" % (longitude, latitude), srid=4326)


        serv_list = list()
        servfilter = Service.objects.filter(point__distance_lte=(pnt, D(mi=50)))
        for serv in servfilter:
            serv_dict = {
                'name':                 serv.name,
                'description':          serv.description,
                'picture':              serv.picture,
                'address':              serv.address,
                'city':                 serv.city,
                'state_or_province':    serv.state_or_province.name,
                'postal_code':          serv.postal_code,
                'country':              serv.country.name,
                'price':                serv.price,
                'get_google_formatted_address': serv.get_google_formatted_address,
                'get_absolute_url':     serv.get_absolute_url(),
                'point': {
                           'x':    serv.point.x,
                           'y':    serv.point.y,
                         },
                }

            serv_list.append(serv_dict)

	jsondata = {'jsondata':serv_list}

        return HttpResponse(simplejson.dumps(jsondata), mimetype="application/json")

@csrf_exempt
def mobile_share(request):
	tourist_name = request.REQUEST["user_name"]
	print tourist_name
	tourist_id = Tourist.objects.get(name = tourist_name).id
	historyfilter = Offer.objects.filter(tourist=tourist_id, date__lte = datetime.date.today(), status = 'F').order_by('date')

	if len(historyfilter) ==0:
		data = {"success_visits": 0}
		return HttpResponse(simplejson.dumps(data), mimetype ="application/json") 

	history_list = list()


	for hist in historyfilter:
		hist_dict ={
			'visit': hist.service.name,
		}
		history_list.append(hist_dict)

	visits_dict = {}
	visits_dict["visits"] = history_list 
		
	visits_dict["success_visits"] = 1
	return HttpResponse(simplejson.dumps(visits_dict), mimetype="application/json")

@csrf_exempt
def mobile_dashboard(request):
	tourist_name = request.REQUEST["user_name"]
	#print tourist_name
	#tourist_name = "Spencer"
	tourist_id = Tourist.objects.get(name = tourist_name).id

	schedulefilter = Offer.objects.filter(tourist=tourist_id, date__gte = datetime.date.today(), status = 'F').order_by('date')
	offerfilter = Offer.objects.filter(tourist=tourist_id).order_by('date').exclude(status= 'F')
	historyfilter = Offer.objects.filter(tourist=tourist_id, date__lte = datetime.date.today(), status = 'F').order_by('date')

	sched_list = list()
	for sched in schedulefilter:
		schedule_dict ={
			'service': sched.service.name,
			'latitude':sched.service.point.y,
			'longitude': sched.service.point.x,
			'date': str(sched.date),
			'time': str(sched.time),
	
		}
		sched_list.append(schedule_dict)

	offer_list = list()
	for off in offerfilter:
		offer_dict ={
			'service': off.service.name,
			'status': off.status,
		}
		offer_list.append(offer_dict)

	history_list = list()
	for hist in historyfilter:
		hist_dict ={
			'service': hist.service.name,
			'date': str(hist.date),
		}
		history_list.append(hist_dict)

	dashboard_dict = { "offer": offer_list, "schedule": sched_list, "history": history_list }
	final_dict = {"dashboard": dashboard_dict, "success": 1}
	return HttpResponse(simplejson.dumps(final_dict), mimetype ="application/json") 


@csrf_exempt
def mobile_comment(request):		
		
	tourist_name  = request.REQUEST["user_name"]
	service_name  = request.REQUEST["visit_name"]
	tips_for_travellers = request.REQUEST["tips_for_travellers"]
	meaningful_exp = request.REQUEST["meaningful_exp"]
	
	t = Tourist.objects.get(name = tourist_name)
	s = Service.objects.get(name = service_name)
	
	M = Mobile()
	M.tips_for_travellers = tips_for_travellers
	M.meaningful_exp = meaningful_exp
	M.tourist = t
	M.service = s
	M.save()		
	final_dict = {"success_upload":1}
	return HttpResponse(simplejson.dumps(final_dict), mimetype ="application/json")
	
@csrf_exempt
def mobile_picture(request):		
	print request
	print "************************************************"
	p = Picture()
	p.picture = request.FILES['photo']
	p.save()
	response_data=[{"success": "1"}]
	return HttpResponse()

def mobile_login(request):		
	tourist_name  = request.REQUEST["user_name"]
	t = Tourist.objects.filter(name = tourist_name)
	if len(t) > 0:
		 return HttpResponse("1", mimetype ="application/json")
	return HttpResponse("0", mimetype ="application/json")


###########################
###       TOURIST       ###
###########################

class TouristDetails(DetailView):
    '''
    '''
    model = Tourist
    pk_url_kwarg = 'tourist_pk'
    context_object_name = 'tourist_detail'
    template_name = 'peoplefirstapp/tourists/detail.html'

    @method_decorator(login_required)
    def dispatch(self, *args, **kwargs):
        return super(TouristDetails, self).dispatch(*args, **kwargs)


def create_tourist(request):
        # render a different template based on what type of user is creating the Entrepreneur
    if request.method == 'GET':
            form = CreateTouristForm()
            return render_to_response('peoplefirstapp/tourists/create.html', {'form': form}, context_instance=RequestContext(request))

    elif request.method == 'POST':
        form = CreateTouristForm(request.POST, request.FILES)

        if form.is_valid():
            cleaned_data = form.cleaned_data
            tourist = Tourist(**form.cleaned_tourist_data)
            email = cleaned_data.get('email', None)
            password = cleaned_data.get('password', None)

            tourist.user = create_user(email, password)
            tourist.save()
            return HttpResponseRedirect(reverse('peoplefirstapp:tourist_detail_url', args=[tourist.pk]))

        else:
            return render_to_response('peoplefirstapp/tourists/create.html', {'form': form}, context_instance=RequestContext(request))

    else:
        return HttpResponseNotAllowed(['GET', 'POST'])


####################
###     OFFER    ###
####################

class OfferList(ListView):
    '''
    '''
    model = Offer
    context_object_name = 'offer_list'
    template_name = 'peoplefirstapp/offers/list.html'

    @method_decorator(login_required)
    def dispatch(self, *args, **kwargs):
        return super(OfferList, self).dispatch(*args, **kwargs)


class OfferDetails(DetailView):
    '''
    '''
    template_name = 'peoplefirstapp/offers/details.html'
    model = Offer
    pk_url_kwarg = 'offer_pk'
    context_object_name = 'offer'

    @method_decorator(login_required)
    def dispatch(self, *args, **kwargs):
        return super(OfferDetails, self).dispatch(*args, **kwargs)


@login_required
def create_offer(request, service_pk):
    '''
    '''
    user = request.user

    if not user.get_profile().is_tourist:
        return HttpResponseForbidden("Only tourists can make an offer on a service.")

    if request.method == 'GET':
        form = OfferForm()
        return render_to_response("peoplefirstapp/offers/create.html", {'form': form, 'service': get_object_or_404(Service, pk=service_pk)}, context_instance=RequestContext(request))
    elif request.method == 'POST':
        form = OfferForm(request.POST)
        if form.is_valid():
            cleaned_data = form.cleaned_data
            offer = Offer(**form.cleaned_data)

            tourist = get_object_or_404(Tourist, user=user)
            service = get_object_or_404(Service, pk=service_pk)

            offer.tourist = tourist
            offer.service = service
            offer.offer_total = service.price * offer.quantity
            queue_offer_text(offer)
            offer.save()

            events.OfferCreate.send(
                str(offer.service.entrepreneur),
                str(offer.service),
                str(offer.tourist),
                datetime.datetime.combine(offer.date, offer.time),
                offer.quantity,
                float(service.price),
            )

            OfferInvalidationTask.apply_async(None, {'offer_pk':offer.pk}, countdown=settings.OFFER_EXPIRE_TIME)

            return HttpResponseRedirect(reverse('peoplefirstapp:offer_detail_url', kwargs={'offer_pk': offer.pk}))
        else:
            return render_to_response('peoplefirstapp/offers/create.html', {'form': form}, context_instance=RequestContext(request))
    else:
        return HttpResponseNotAllowed(['GET', 'POST'])


def cancel_offer(request, offer_pk):
    '''
    '''
    offer = Offer.objects.get(pk=offer_pk)
    if request.method == 'POST':
        offer.status = 'C'
        offer.save()
        events.OfferCancel.send(
            str(offer.service.entrepreneur),
            str(offer.service),
            str(offer.tourist),
        )
        return HttpResponseRedirect(offer.get_absolute_url())
    else:
        return render_to_response("peoplefirstapp/offers/cancel.html", {'service_name': offer.service.name}, context_instance=RequestContext(request))


@csrf_exempt
def checkout(request, offer_pk):
    '''

    We use Spreedlycore (https://spreedlycore.com/) for payment.  This allows
    us to handle transactions without touching any sensitive information.  Here
    is how it works:

    1. The user enters information on our website which gets POSTed to
        spreedly's site.

    2. Spreedly redirects the user to our payment_complete url, adding a
        payment token to the GET request data.

    3. Inside the payment_complete view, we use the token to make a request
        back to spreedly, which actually makes the payment.

    '''
    offer = get_object_or_404(Offer, pk=offer_pk, status='A')

    if offer.service.entrepreneur.payment_gateway_token is not None and len(offer.service.entrepreneur.payment_gateway_token) > 0:
        return render_to_response("peoplefirstapp/checkout.html", {
            'offer_id': offer_pk,
            'redirect_url': request.build_absolute_uri(reverse('peoplefirstapp:payment_complete_url', kwargs={'offer_pk': offer_pk})),
            'api_login': settings.SPREEDLYCORE_API_LOGIN,
        }, context_instance=RequestContext(request))
    elif offer.service.entrepreneur.paypal_email is not None and len(offer.service.entrepreneur.paypal_email) > 0:

        root_url = request.get_host()
        print request.build_absolute_uri("/offers/payment_validation/ipn/")
        paypal_dict = {
            "business": offer.service.entrepreneur.paypal_email,
            "amount": offer.offer_total,
            "item_name": offer.service.name,
            "invoice": "%s-%s" % (offer.tourist.name, offer_pk),
            "notify_url": request.build_absolute_uri("/offers/payment_validation/ipn/"),
            "return_url": request.build_absolute_uri("/offers/%s/success/" % offer_pk),
            "cancel_return": request.build_absolute_uri("/offers/%s/canceled/" % offer_pk),
            "cbt": "Return to People-First Tourism",
        }

        # Create the instance.
        form = PayPalPaymentsForm(initial=paypal_dict)
        context = {"form": form}
        return render_to_response("peoplefirstapp/paypal_checkout.html", {"form": form}, context_instance=RequestContext(request))

    else:
        return HttpResponse("Error: seller has no payment method set up.")


@csrf_exempt  # this view is loaded by PayPal so it won't receive any csrf token
def paypal_canceled(request, offer_pk):
    return HttpResponseRedirect(reverse('peoplefirstapp:offer_detail_url', kwargs={'offer_pk': offer_pk}))


@csrf_exempt  # this view is loaded by PayPal so it won't receive any csrf token
def paypal_success(request, offer_pk):
    return HttpResponseRedirect(reverse('peoplefirstapp:offer_detail_url', kwargs={'offer_pk': offer_pk}))


def payment_complete(request, offer_pk):
    '''
    '''
    offer = get_object_or_404(Offer, pk=offer_pk, status='A')

    if 'token' in request.GET:
        payment_token = request.GET['token']
    elif 'error' in request.GET:
        return HttpResponse("Error: {0}".format(request.GET['error']))
    else:
        return HttpResponse("Error in payment: Payment Token Missing")

    # Request payment from spreedly
    s = Spreedly(settings.SPREEDLYCORE_API_LOGIN,
                 settings.SPREEDLYCORE_API_SECRET,
                 offer.service.entrepreneur.payment_gateway_token)
    amount = offer.quantity * offer.service.price
    payment_response = s.process_payment(amount, payment_token)

    if payment_response.success:
        # Update Offer
        offer.status = 'F'
        #TODO: Make confirmation number random
        confirmation_number = hex(offer.pk).replace('0x', '').zfill(7)
        offer.confirmation_number = confirmation_number
        offer.save()

        # Confirmation email to tourist
        html_content = "Your payment for " + offer.service.name + " has been accepted. Your confirmation number is " + confirmation_number + "."
        send_email(offer.tourist.user.email, 'People First Tourism Payment Complete', html_content)

        # Confirmation text to entrepreneur
        send_confirmation_text(offer)

        messages.add_message(request, messages.SUCCESS, 'Payment successfully completed.')
        return HttpResponseRedirect(offer.get_absolute_url())

    else:
        return render_to_response("peoplefirstapp/checkout.html", {
            'offer_id': offer_pk,
            'redirect_url': request.build_absolute_uri(reverse('peoplefirstapp:payment_complete_url', kwargs={'offer_pk': offer_pk})),
            'api_login': settings.SPREEDLYCORE_API_LOGIN,
            'errors': payment_response.errors,
            'fields': payment_response.fields,
            'response': payment_response,
        }, context_instance=RequestContext(request))


######################
###     SERVICE    ###
######################

class ServiceList(TemplateView):
    '''
    '''
    template_name = 'peoplefirstapp/services/list.html'
    
    @csrf_exempt
    def dispatch(self, *args, **kwargs):
        return super(ServiceList, self).dispatch(*args, **kwargs)
    
    def get_json_data(self):
        # the JSON objects are built here manually as opposed to using an __iter__()
        # method as a helper to dict(), since the __iter__() method would need to be
        # generic, and include data not relevant to this view.
        c_list = list()
        for c in Country.objects.all():

            if c.marker_location is None:
                continue 

            c_dict = {
                'country_code':     c.country_code,
                'name':             c.name,
                'marker_location': {
                    'x':    c.marker_location.x,
                    'y':    c.marker_location.y,
                },
            }

            s_list = list()
            for s in c.states_provinces:
 
             if s.marker_location is None:
                continue 

                s_dict = {
                    'code':             s.code,
                    'country_code':     s.country.code,
                    'sub_code':         s.sub_code,
                    'name':             s.name,
                    'type':             s.type,
                    'marker_location': {
                        'x':    s.marker_location.x,
                        'y':    s.marker_location.y,
                    },
                }

                n_list = list()
                for n in s.networks:

                    if n.marker_location is None:
                        continue 

                    n_dict = {
                        'name':     n.name,
                        'marker_location': {
                            'x':    n.marker_location.x,
                            'y':    n.marker_location.y,
                        },
                    }

                    serv_list = list()
                    for serv in n.services:
                        serv_dict = {
                            'name':                 serv.name,
                            'description':          serv.description,
                            'picture':              serv.picture,
                            'address':              serv.address,
                            # 'address_comments':     serv.address_comments,
                            'city':                 serv.city,
                            'state_or_province':    serv.state_or_province.name,
                            'postal_code':          serv.postal_code,
                            'country':              serv.country.name,
                            'price':                serv.price,
                            #'get_google_formatted_address': serv.get_google_formatted_address,
                            #'get_absolute_url':     serv.get_absolute_url(),
                            'point': {
                                'x':    serv.point.x,
                                'y':    serv.point.y,
                            },
                        }

                        serv_list.append(serv_dict)

                    n_dict['services'] = serv_list
                    n_list.append(n_dict)

                s_dict['networks'] = n_list
                s_list.append(s_dict)

            c_dict['states_provinces'] = s_list
            c_list.append(c_dict)

        return simplejson.dumps(c_list)

    def post(self, request, *args, **kwargs):
        context = self.get_context_data(**kwargs)
        return self.render_to_response(context)

    def get_context_data(self, **kwargs):
        context = super(ServiceList, self).get_context_data(**kwargs)
        
        print self.request.method
        if self.request.POST.get('state_id') is not None:
            context['post_state_id'] = self.request.POST.get('state_id')
        if self.request.POST.get('country_id') is not None:
            context['post_country_id'] = self.request.POST.get('country_id')
   
        context['country_list'] = self.get_json_data()
        context['service_list'] = Service.objects.all()
        return context


def handle403(request):
    return render(request, '403.html')


def handle404(request):
    return render(request, '404.html')


def handle413(request):
    return render(request, 'peoplefirstapp/413.html')


def handle500(request):
    return render(request, '500.html')
