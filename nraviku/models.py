######################################################################################
# Django is an ORM and support many databases whose models are specified here  	     #
# The models are labelled and serve specific database requirements of the application#
######################################################################################

from django.contrib.gis.db import models
from django.contrib.gis import geos
from django.contrib import messages
from django.core.validators import RegexValidator
from django.contrib.auth.models import User
from django.core.mail import EmailMessage
from django.dispatch import receiver
from django.utils.translation import ugettext_lazy as _
from django.conf import settings
from django.contrib.auth.models import User
from django.db.models.signals import post_save, post_delete
from django.shortcuts import get_object_or_404
from cgi import escape
from datetime import datetime
from peoplefirstapp.utils import send_email
from paypal.standard.ipn.signals import payment_was_successful, payment_was_flagged
from offline_messages.utils import create_offline_message, constants


from django.core.files.storage import FileSystemStorage
fs = FileSystemStorage(location=settings.MEDIA_ROOT)
import pycountry
import logging

from peoplefirstapp import events

######################
###     Models     ###
######################

class UserProfile(models.Model):

    user = models.OneToOneField(User)

    @property
    def is_admin(self):
        '''
        '''
        return len(PeopleFirstAdmin.objects.filter(user=self.user)) > 0

    @property
    def get_admin(self):
        '''
        '''
        return PeopleFirstAdmin.objects.filter(user=self.user)[0]

    @property
    def is_empowerment_agent(self):
        '''
        '''
        return len(EmpowermentAgent.objects.filter(user=self.user)) > 0

    @property
    def get_agent(self):
        '''
        '''
        return EmpowermentAgent.objects.filter(user=self.user)[0]

    def in_network(self, network):
        if self.is_empowerment_agent:
            return network in self.get_agent.networks.all()
        return False

    @property
    def is_entrepreneur(self):
        '''
        '''
        return len(Entrepreneur.objects.filter(user=self.user)) > 0

    @property
    def is_tourist(self):
        '''
        '''
        return len(Tourist.objects.filter(user=self.user)) > 0

    @property
    def get_tourist(self):
        '''
        '''
        return Tourist.objects.filter(user=self.user)[0]

    @property
    def offers(self):
        """
        Returns a list of the ``Offer``s the ``Tourist`` has made.
        """
        return Offer.objects.filter(tourist__user=self.user)

    def create_user_profile(sender, instance, created, **kwargs):
        if created:
            UserProfile.objects.create(user=instance)

    post_save.connect(create_user_profile, sender=User)

##########################
###   User Profile     ###
##########################

class PeopleFirstUser(models.Model):
    """
    A ``PeopleFirstUser`` is an abstract model that all user types extend.
    """
    user        = models.ForeignKey(User, blank=True, null=True)

    name        = models.CharField(_(u'name'), max_length=64)

    _picture    = models.ImageField(_(u'picture'), upload_to='admins', storage=fs, db_column='picture', blank=True, null=True)

    def get_picture(self):
        """
        Gets the users picture or the 'no image available' picture if
        it doesn't exist.
        """
        if self._picture.name is None or len(self._picture.name) < 1:
            return '%s%s' % (settings.STATIC_URL, 'peoplefirstapp/img/no-image.png')
        else:
            return '/media/%s' % self._picture.name

    def set_picture(self, input):
        self._picture = input

    picture = property(get_picture, set_picture)

    class Meta:
        abstract = True


class Tourist(PeopleFirstUser):
    class Meta:
        verbose_name = 'Tourist'
        verbose_name_plural = 'Tourists'

    def __unicode__(self):
        return unicode(self.name)

    def get_absolute_url(self):
        return unicode('/tourists/%i/' % self.pk)



#################################
# Franchise- entrepreneur group #
#################################

class Franchise(models.Model):
    """
    A ``Franchise`` is a branded group of ``Network``s in the P1T system.
    """
    name            = models.CharField(_(u'name'), max_length=64)
    description     = models.TextField(_(u'description'), max_length=1000)
    picture         = models.ImageField(_(u'picture'), upload_to='franchises', storage=fs)

    max_services    = models.IntegerField(_(u'max services'))

    class Meta:
        verbose_name = 'Franchise'
        verbose_name_plural = 'Franchises'

    @property
    def networks(self):
        """
        Returns a list of the ``Network``s in the franchise.
        """
        return Network.objects.filter(franchises__id=self.id)

    @property
    def agents(self):
        """
        Returns a list of the ``EmpowermentAgent``s in the franchise.
        """
        #this needs to be replaced with a Django filter
        agent_list = set()

        for network in self.networks:
            for agent in network.agents:
                agent_list.add(agent)

        return agent_list

    def __unicode__(self):
        return unicode(self.name)

    @models.permalink
    def get_absolute_url(self):
        return ('peoplefirstapp:franchise_detail_url', [self.pk])


######################
### Service Models     ###
######################



class Service(models.Model):
    """
    A service provided to ``Tourist``s by ``Entrepreneur``s.
    """
    name                = models.CharField(_(u'name'), max_length=64)
    description         = models.TextField(_(u'description'), max_length=1000)

    _picture = models.ImageField(_(u'picture'), upload_to='franchises', storage=fs, db_column='picture', blank=True, null=True)

    def get_picture(self):
        """
        Gets the service's picture or the 'no image available' picture if
        it doesn't exist.
        """
        if len(self._picture.name) < 1:
            return self.entrepreneur.picture

        return '/media/%s' % self._picture

    def set_picture(self, input):
        self._picture = input

    picture             = property(get_picture, set_picture)

    address             = models.CharField(_(u'street address'), max_length=128)
    address_comments    = models.TextField(_(u'address comments'), blank=True)
    city                = models.CharField(_(u'city'), max_length=64)
    state_or_province   = models.ForeignKey(StateProvince)
    postal_code         = models.CharField(_(u'postal code'), max_length=32, blank=True)
    country             = models.ForeignKey(Country)
    # state_or_province   = models.CharField(_(u'state or province'), max_length=64)
    # state_or_province_temp = models.CharField(_(u'state or province name'), max_length=64, null=True)
    # country             = models.CharField(_(u'country'), max_length=64)
    # country_code        = models.CharField(_(u'country code'), max_length=2)

    point               = models.PointField()

    price               = models.DecimalField(_(u'price'), max_digits=6, decimal_places=2)

    entrepreneur        = models.ForeignKey(Entrepreneur)

    objects             = models.GeoManager()

    class Meta:
        verbose_name = 'Service'
        verbose_name_plural = 'Services'

    def __unicode__(self):
        return unicode(self.name)

    @property
    def get_google_formatted_address(self):
        return '%s+%s+%s+%s' % (self.address.replace(' ', '+'),
                                self.city.replace(' ', '+'),
                                self.state_or_province.name.replace(' ', '+'),
                                self.postal_code)

    @models.permalink
    def get_absolute_url(self):
        return ('peoplefirstapp:service_detail_url', [self.pk])

    @property
    def get_links_url(self):
        return '%slinks' % self.get_absolute_url()
        

class GoogleLocation(models.Model):
    """
    Represents a location with all possible fields provided by the Google Maps
    API reverse geocoding (i.e. returning location information given a latitude
    and longitude). Contains a large amount of possibly extraneous information,
    but supports broad international conventions of addresses and location
    information.

    Not currently used.
    """
    street_address              = models.CharField(_(u'street address'), max_length=16)
    route                       = models.CharField(_(u'route'), max_length=128)
    intersection                = models.CharField(_(u'intersection'), max_length=128)
    political                   = models.CharField(_(u'political'), max_length=128)
    country                     = models.CharField(_(u'country'), max_length=128)
    administrative_area_level_1 = models.CharField(_(u'administrative area level 1'), max_length=128)
    administrative_area_level_2 = models.CharField(_(u'administrative area level 2'), max_length=128)
    administrative_area_level_3 = models.CharField(_(u'administrative area level 3'), max_length=128)
    colloquial_area             = models.CharField(_(u'colloquial area'), max_length=128)
    locality                    = models.CharField(_(u'locality'), max_length=128)
    sublocality                 = models.CharField(_(u'sublocality'), max_length=128)
    neighborhood                = models.CharField(_(u'neighborhood'), max_length=128)
    premise                     = models.CharField(_(u'premise'), max_length=128)
    subpremise                  = models.CharField(_(u'subpremise'), max_length=128)
    postal_code                 = models.CharField(_(u'postal code'), max_length=128)
    natural_feature             = models.CharField(_(u'natural feature'), max_length=128)
    airport                     = models.CharField(_(u'airport'), max_length=128)
    park                        = models.CharField(_(u'park'), max_length=128)
    point_of_interest           = models.CharField(_(u'point of interest'), max_length=128)
    locality                    = models.CharField(_(u'locality'), max_length=128)
    latlong                     = models.PointField()

    class Meta:
        verbose_name = 'Google Location'
        verbose_name_plural = 'Google Locations'


class Mobile(models.Model):
    tourist             = models.ForeignKey(Tourist)
    service             = models.ForeignKey(Service)
    tips_for_travellers	= models.CharField(_(u'tips for travellers'), max_length = 300, null = True)
    meaningful_exp	= models.CharField(_(u'meaningful exp'), max_length = 300, null = True)
    date_added          = models.DateTimeField(_(u'date added'), default=datetime.today())

    class Meta:
        verbose_name = 'Mobile'
        verbose_name_plural = 'Mobiles'

class Picture(models.Model):
    picture    = models.ImageField(_(u'picture'), upload_to='mobile', storage=fs, db_column='picture', blank=True, null=True)

class Offer(models.Model):
    """
    A proposed purchase of services by a ``Tourist`` from an ``Entrepreneur``.
    The ``Entrepreneur`` may choose to accept or decline the ``Offer``; if it
    is accepted, the ``Tourist`` will then provide payment.
    """
    STATUS_CHOICES = (
        ('P', 'Pending'),
        ('A', 'Accepted'),
        ('D', 'Declined'),
        ('C', 'Canceled'),
        ('F', 'Paid'),
    )

    tourist             = models.ForeignKey(Tourist)
    service             = models.ForeignKey(Service)
    quantity            = models.IntegerField(_(u'quantity'))
    date                = models.DateField(_(u'date'))
    time                = models.TimeField(_(u'time'))
    sent                = models.BooleanField(editable=False)
    status              = models.CharField(_(u'status'), max_length=1, choices=STATUS_CHOICES)
    confirmation_number = models.CharField(_(u'confirmation number'), max_length=64, null=True)
    offer_total         = models.DecimalField(_(u'offer total'), max_digits=9, decimal_places=2, null=True)

    date_created        = models.DateTimeField(_(u'date created'), default=datetime.today())
    date_paid           = models.DateTimeField(_(u'date paid'), blank=True, null=True)

    # the IPN table stores a large amount of data PayPal returns on completion of payment
    paypal_ipn_data     = models.ForeignKey('ipn.PayPalIPN', blank=True, null=True)

    class Meta:
        verbose_name = 'Offer'
        verbose_name_plural = 'Offers'

    def __unicode__(self):
        offer = ''
        status = self.status
        datetime_str = datetime.combine(self.date, self.time).strftime('%b %d, \'%y, %H:%M')
        confirmation_number = self.confirmation_number
        if status == 'P':
            offer = "OFFER || " + self.service.name + " || " + datetime_str + " || Qty: " + str(self.quantity)
        elif status == 'F':
            offer = "PAID || " + self.service.name + " || " + datetime_str + " || Qty: " + str(self.quantity) + " || #" + confirmation_number
        elif status == 'A':
            offer = "ACCEPTED || " + self.service.name + " || " + datetime_str + " || Qty: " + str(self.quantity)
        elif status == 'D':
            offer = "DECLINED || " + self.service.name + " || " + datetime_str + " || Qty: " + str(self.quantity)
        elif status == 'C':
            offer = "CANCELED || " + self.service.name + " || " + datetime_str + " || Qty: " + str(self.quantity)
        return unicode(offer)

    def payment_statement(self):
        amount = self.service.price * self.quantity
        service_name = self.service.name
        entrepreneur = self.service.user.name
        return unicode(amount + " is being sent to " + entrepreneur + " for " + service_name + " on " + self.datetime)

    @models.permalink
    def get_absolute_url(self):
        return ('peoplefirstapp:offer_detail_url', [self.pk])

    @property
    def get_links_url(self):
        return '%slinks' % self.get_absolute_url()


class Activation(models.Model):
    user            = models.ForeignKey(User)

    activation_key  = models.CharField(max_length=100, blank=True, null=True)

    class Meta:
        verbose_name = 'Activation'
        verbose_name_plural = 'Activations'


@receiver(post_save, sender=Service)
@receiver(post_delete, sender=Service)
def update_locations(sender, instance, **kwargs):
    """Update location markers affected by adding/removing services."""

    instance.entrepreneur.network.update_location()
    instance.state_or_province.update_location()
    instance.country.update_location()
