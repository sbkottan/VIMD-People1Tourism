############################################################################
# This is the ADMIN page that allows easy access to the data in the models #
# The ADMIN entry is created for every model present in the models.py      #
############################################################################

from django.contrib.gis import admin

from peoplefirstapp.models import Tourist, PeopleFirstAdmin, Franchise, Network, EmpowermentAgent, Entrepreneur, Service, PhoneNumber, Offer, Mobile


class PeopleFirstAdminAdmin(admin.GeoModelAdmin):
    pass


class FranchiseAdmin(admin.GeoModelAdmin):
    pass


class NetworkAdmin(admin.GeoModelAdmin):
    pass


class TouristAdmin(admin.GeoModelAdmin):
    pass


class PhoneNumberAdmin(admin.GeoModelAdmin):
    pass

class OfferAdmin(admin.GeoModelAdmin):
   pass

class MobileAdmin(admin.GeoModelAdmin):
   pass
   

class EmpowermentAgentAdmin(admin.OSMGeoAdmin):
    default_lon = -78.6389
    default_lat = 35.7719
    openlayers_url = '/static/peoplefirstapp/js/libs/openlayers/OpenLayers-2.12/OpenLayers.js'

    fields = ('user', 'name', 'description', 'city', 'state_or_province',
              'country', 'phone_number', '_picture', 'point', 'networks')

class EntrepreneurAdmin(admin.OSMGeoAdmin):
    def queryset(self, request):
        qs = super(EntrepreneurAdmin, self).queryset(request)
        if request.user.is_superuser or request.user.is_staff:
            return qs
        return qs.filter(user=request.user)

    def save_model(self, request, obj, form, change):
        if change == False:
            obj.user = request.user
            obj.save()
        obj.save()

    default_lon = -78.6389
    default_lat = 35.7719
    openlayers_url = '/static/peoplefirstapp/js/libs/openlayers/OpenLayers-2.12/OpenLayers.js'

    fields = ('name', 'description', 'address', 'address_comments', 'city',
              'state_or_province', 'country', 'phone_number', 'point', 'limit_sms',
              'maxsms', 'payment_gateway_token', 'paypal_email', '_picture', 'network')


class ServiceAdmin(admin.OSMGeoAdmin):
    def queryset(self, request):
        qs = super(ServiceAdmin, self).queryset(request)
        if request.user.is_superuser or request.user.is_staff:
            return qs
        return qs.filter(entrepreneur__user=request.user)

    def save_model(self, request, obj, form, change):
        if change == False:
            obj.user = request.user
            obj.save()
        obj.save()

    def formfield_for_foreignkey(self, db_field, request, **kwargs):
        if db_field.name == "entrepreneur":
            if request.user.is_superuser or request.user.is_staff:
                kwargs["queryset"] = Entrepreneur.objects.all()
            else:
                kwargs["queryset"] = Entrepreneur.objects.filter(user=request.user)
        return super(ServiceAdmin, self).formfield_for_foreignkey(db_field, request, **kwargs)

    default_lon = -78.6389
    default_lat = 35.7719
    openlayers_url = '/static/peoplefirstapp/js/libs/openlayers/OpenLayers-2.12/OpenLayers.js'

    fields = ('name', 'description', '_picture', 'address', 'city', 'state_or_province', 'country', 'postal_code', 'point', 'price', 'entrepreneur')


admin.site.register(PeopleFirstAdmin, PeopleFirstAdminAdmin)
admin.site.register(Franchise, FranchiseAdmin)
admin.site.register(PhoneNumber, PhoneNumberAdmin)
admin.site.register(Network, NetworkAdmin)
admin.site.register(Tourist, TouristAdmin)
admin.site.register(EmpowermentAgent, EmpowermentAgentAdmin)
admin.site.register(Entrepreneur, EntrepreneurAdmin)
admin.site.register(Service, ServiceAdmin)
admin.site.register(Offer, OfferAdmin)
admin.site.register(Mobile, MobileAdmin)
